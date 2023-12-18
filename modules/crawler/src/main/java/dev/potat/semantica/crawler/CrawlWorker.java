package dev.potat.semantica.crawler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.*;
import dev.potat.semantica.common.CrawlRequest;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.ThreadLocalRandom;

public class CrawlWorker {
    public static final String QUEUE_NAME = "crawl_tasks";

    public static void main(String[] args) {
        Crawler crawler = new Crawler();
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(System.getenv("RABBITMQ_HOST"));
        factory.setUsername(System.getenv("RABBITMQ_USERNAME"));
        factory.setPassword(System.getenv("RABBITMQ_PASSWORD"));

        try (Connection connection = factory.newConnection(); Channel channel = connection.createChannel()) {
            channel.queueDeclare(QUEUE_NAME, true, false, false, null);

            Consumer consumer = new DefaultConsumer(channel) {
                @Override
                public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) {
                    String message = new String(body, StandardCharsets.UTF_8);
                    System.out.println("Received: " + message);

                    try {
                        ObjectMapper objectMapper = new ObjectMapper();
                        CrawlRequest task = objectMapper.readValue(message, CrawlRequest.class);
                        System.out.println("Crawling: " + task);
                        Thread.sleep(ThreadLocalRandom.current().nextInt(1000, 5000));
                    } catch (JsonProcessingException | InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            };

            channel.basicConsume(QUEUE_NAME, true, consumer);
            System.out.println("Connected");

            // Keep the consumer running until interrupted
            Thread.sleep(Long.MAX_VALUE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
