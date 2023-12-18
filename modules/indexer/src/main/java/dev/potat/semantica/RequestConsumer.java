package dev.potat.semantica;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import dev.potat.semantica.common.CrawlRequest;
import dev.potat.semantica.common.MongoWrapper;
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

    public RequestConsumer(Channel channel, MongoWrapper mongo) {
        super(channel);
        this.channel = channel;
        this.db = mongo;
    }

    @Override
    public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) {
        String message = new String(body, StandardCharsets.UTF_8);

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            CrawlRequest request = objectMapper.readValue(message, CrawlRequest.class);
            System.out.println("Crawling: " + request);

            // process request
            db.saveLinkToDB(request.getUrl());
            Document document = Jsoup.connect(request.getUrl()).get();
            URL rootUrl = new URL(request.getUrl());
            System.out.println(rootUrl.getHost());
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
            }

            Thread.sleep(ThreadLocalRandom.current().nextInt(100, 200));
        } catch (InterruptedException | IOException e) {
            throw new RuntimeException(e);
        }
    }
}
