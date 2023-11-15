package dev.potat.semantica;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.*;

import java.nio.charset.StandardCharsets;

public class Crawler {
    public static final String QUEUE_NAME = "crawl_tasks";

    public static void main(String[] args) {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        factory.setUsername("test");
        factory.setPassword("test");

        try (Connection connection = factory.newConnection();
             Channel channel = connection.createChannel()) {
            channel.queueDeclare(QUEUE_NAME, false, false, false, null);

            Consumer consumer = new DefaultConsumer(channel) {
                @Override
                public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) {
                    String message = new String(body, StandardCharsets.UTF_8);
                    System.out.println("Received: " + message);

                    try {
                        ObjectMapper objectMapper = new ObjectMapper();
                        CrawlTask task = objectMapper.readValue(message, CrawlTask.class);
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
