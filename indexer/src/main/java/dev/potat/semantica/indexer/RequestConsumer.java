package dev.potat.semantica.indexer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import dev.potat.semantica.common.CrawlRequest;
import dev.potat.semantica.common.MongoWrapper;
import dev.potat.semantica.common.UrlCleaner;
import dev.potat.semantica.common.embeddings.DocumentSplitter;
import dev.potat.semantica.common.embeddings.EmbeddingsExtractor;
import dev.potat.semantica.common.embeddings.MilvusWrapper;
import dev.potat.semantica.common.keywords.Keywords;
import dev.potat.semantica.common.keywords.KeywordsExtractor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class RequestConsumer extends DefaultConsumer {
    MongoWrapper db;
    MilvusWrapper vectorDB;
    Channel channel;
    KeywordsExtractor keywordsExtractor;
    DocumentSplitter documentSplitter;
    EmbeddingsExtractor embeddingsExtractor;

    public RequestConsumer(Channel channel, MongoWrapper mongo, MilvusWrapper milvus, KeywordsExtractor keywordsExtractor, DocumentSplitter documentSplitter, EmbeddingsExtractor embeddingsExtractor) {
        super(channel);
        this.channel = channel;
        this.db = mongo;
        this.vectorDB = milvus;
        this.keywordsExtractor = keywordsExtractor;
        this.documentSplitter = documentSplitter;
        this.embeddingsExtractor = embeddingsExtractor;
    }

    @Override
    public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) {
        String message = new String(body, StandardCharsets.UTF_8);

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            CrawlRequest request = objectMapper.readValue(message, CrawlRequest.class);
            System.out.println("Crawling: " + request);

            // process request
            String requestUrl;
            String host;
            try {
                requestUrl = UrlCleaner.clean(request.getUrl());
                host = new URI(requestUrl).getHost();
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("Could not process url: " + request.getUrl());
                return;
            }

            System.out.println("Crawling: " + requestUrl);
            if (db.checkLinkExists(requestUrl)) {
                System.out.println("Already Exists! Skipping...");
                return;
            }
            db.saveLinkToDB(requestUrl);

            Document document = Jsoup.connect(requestUrl).get();
//            String webpage = document.html();
//            System.out.println(webpage);

            if (request.getCurrentDepth() < request.getMaxDepth()) {
                Elements links = document.select(request.getLinkSelector());
                ArrayList<String> urlsToCrawl = new ArrayList<>();
                for (Element link : links) {
                    URL currentUrl = new URL(link.absUrl("href"));
                    if (currentUrl.getHost().endsWith(host)) {
//                        System.out.println(currentUrl);
                        urlsToCrawl.add(currentUrl.toString());
                    }
                }
                ArrayList<String> filteredUrls = db.filterExistingLinks(urlsToCrawl);
                filteredUrls.remove(requestUrl); // test
                System.out.println("Filtered URLS:");
                for (String url : filteredUrls) {
                    System.out.println(url);
                    CrawlRequest newRequest = CrawlRequest.builder()
                            .url(url)
                            .textSelector(request.getTextSelector())
                            .linkSelector(request.getLinkSelector())
                            .maxDepth(request.getMaxDepth())
                            .currentDepth(request.getCurrentDepth() + 1)
                            .build();
                    channel.basicPublish("", Worker.QUEUE_NAME, null,
                            objectMapper.writeValueAsBytes(newRequest));
                }
            } else {
                System.out.println("Max recursion depth reached. Indexing, but not crawling next");
            }

            /// Main Processing

            String text = document.select(request.getTextSelector()).text();
            if (text.isEmpty()) {
                System.out.println("Could not find selector. Skipping...");
                return;
            }

            System.out.println("\n\n\n--------------------\n\n\n");
            System.out.println(text);

            Keywords keywords = keywordsExtractor.extractKeywords(text);
            db.saveKeywordsForURL(requestUrl, keywords.simpleFilter(1, 24));
            System.out.println("Inserted Keywords to Mongo");

            List<String> segments = documentSplitter.split(text);
            System.out.println("Segments count: " + segments.size());

            List<List<Float>> embeddings = new ArrayList<>();
            for (String segment : segments) {
                System.out.println(segment);
                System.out.println("\n--------\n");
                try {
                    List<Float> embedding = embeddingsExtractor.extract(segment);
                    embeddings.add(embedding);
                } catch (Exception e) {
                    e.printStackTrace();
                    System.out.println("Cant process segment:");
                    System.out.println(segment);
                }
            }
            System.out.println("Processed All Embeddings");

            vectorDB.insertEmbeddings(requestUrl, embeddings);
            System.out.println("Inserted Embeddings to Milvus");

            Thread.sleep(ThreadLocalRandom.current().nextInt(100, 200));
            db.setReady(requestUrl);
        } catch (InterruptedException | IOException e) {
            throw new RuntimeException(e);
        }
    }
}
