package dev.potat.semantica.crawler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.*;
import dev.potat.semantica.common.CrawlRequest;

import java.nio.charset.StandardCharsets;

public class Crawler {
    public static final String QUEUE_NAME = "crawl_tasks";

    public static void main(String[] args) {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(System.getenv("RABBITMQ_HOST"));
        factory.setUsername(System.getenv("RABBITMQ_USERNAME"));
        factory.setPassword(System.getenv("RABBITMQ_PASSWORD"));

        try (Connection connection = factory.newConnection(); Channel channel = connection.createChannel()) {
            channel.queueDeclare(QUEUE_NAME, false, false, false, null);

            Consumer consumer = new DefaultConsumer(channel) {
                @Override
                public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) {
                    String message = new String(body, StandardCharsets.UTF_8);
                    System.out.println("Received: " + message);

                    try {
                        ObjectMapper objectMapper = new ObjectMapper();
                        CrawlRequest task = objectMapper.readValue(message, CrawlRequest.class);
                        System.out.println("Crawling: " + task);
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException(e);
                    }
                }
            };

            channel.basicConsume(QUEUE_NAME, true, consumer);

            // Keep the consumer running until interrupted
            Thread.sleep(Long.MAX_VALUE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
