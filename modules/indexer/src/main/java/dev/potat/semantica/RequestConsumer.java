package dev.potat.semantica;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import dev.potat.semantica.common.CrawlRequest;
import dev.potat.semantica.common.MongoWrapper;
import dev.potat.semantica.common.keywords.Keywords;
import dev.potat.semantica.common.keywords.KeywordsExtractor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;

public class RequestConsumer extends DefaultConsumer {
    MongoWrapper db;
    Channel channel;
    KeywordsExtractor keywordsExtractor;

    public RequestConsumer(Channel channel, MongoWrapper mongo, KeywordsExtractor keywordsExtractor) {
        super(channel);
        this.channel = channel;
        this.db = mongo;
        this.keywordsExtractor = keywordsExtractor;
    }

    @Override
    public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) {
        String message = new String(body, StandardCharsets.UTF_8);

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            CrawlRequest request = objectMapper.readValue(message, CrawlRequest.class);
            System.out.println("Crawling: " + request);

            // process request
            URL rootUrl;
            db.saveLinkToDB(request.getUrl());
            try {
                rootUrl = new URL(request.getUrl());
                System.out.println("Domain name: " + rootUrl.getHost());
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("Could not process url: " + request.getUrl());
                return;
            }

            Document document = Jsoup.connect(request.getUrl()).get();
//            String webpage = document.html();
//            System.out.println(webpage);

            if (request.getCurrentDepth() < request.getMaxDepth()) {
                Elements links = document.select(request.getLinkSelector());
                ArrayList<String> urlsToCrawl = new ArrayList<>();
                for (Element link : links) {
                    URL currentUrl = new URL(link.absUrl("href"));
                    if (currentUrl.getHost().endsWith(rootUrl.getHost())) {
//                        System.out.println(currentUrl);
                        urlsToCrawl.add(currentUrl.toString());
                    }
                }
                ArrayList<String> filteredUrls = db.filterExistingLinks(urlsToCrawl);
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

            Elements textElements = document.select(request.getTextSelector());
            StringBuilder stringBuilder = new StringBuilder();
            for (Element e : textElements) {
                stringBuilder.append(e.text()).append("\n\n");
            }

            Keywords keywords = keywordsExtractor.extractKeywords(stringBuilder.toString());
            db.saveKeywordsForURL(request.getUrl(), keywords.simpleFilter(1, 24));

            Thread.sleep(ThreadLocalRandom.current().nextInt(100, 200));
            db.setReady(request.getUrl());
        } catch (InterruptedException | IOException e) {
            throw new RuntimeException(e);
        }
    }
}
