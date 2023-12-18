package dev.potat.semantica;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.*;
import dev.potat.semantica.common.IndexingRequest;
import dev.potat.semantica.common.MongoWrapper;
import dev.potat.semantica.common.keywords.KeywordsExtractor;
import dev.potat.semantica.common.keywords.Pipeline;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.ThreadLocalRandom;

public class Worker {
    public static final String QUEUE_NAME = "CRAWL_TASKS";

    public static void main(String[] args) {
        MongoWrapper mongo = MongoWrapper.getInstance(System.getenv("MONGODB_URI"));
        KeywordsExtractor keywordsExtractor = KeywordsExtractor.builder().pipeline(Pipeline.getPipeline()).build();

        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(System.getenv("RABBITMQ_HOST"));
        factory.setUsername(System.getenv("RABBITMQ_USERNAME"));
        factory.setPassword(System.getenv("RABBITMQ_PASSWORD"));

        try (Connection connection = factory.newConnection(); Channel channel = connection.createChannel()) {
            channel.queueDeclare(QUEUE_NAME, true, false, false, null);
            Consumer consumer = new RequestConsumer(channel, mongo, keywordsExtractor);
            channel.basicConsume(QUEUE_NAME, true, consumer);
            System.out.println("Connected");

            // Keep the consumer running until interrupted
            Thread.sleep(Long.MAX_VALUE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
