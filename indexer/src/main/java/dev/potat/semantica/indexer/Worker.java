package dev.potat.semantica.indexer;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Consumer;
import dev.potat.semantica.common.keywords.MongoWrapper;
import dev.potat.semantica.common.embeddings.DocumentSplitter;
import dev.potat.semantica.common.embeddings.EmbeddingsExtractor;
import dev.potat.semantica.common.embeddings.MilvusWrapper;
import dev.potat.semantica.common.keywords.KeywordsExtractor;
import dev.potat.semantica.common.keywords.Pipeline;

public class Worker {
    public static final String QUEUE_NAME = "CRAWL_TASKS";

    public static void main(String[] args) {
        MongoWrapper mongo = MongoWrapper.getInstance(System.getenv("MONGODB_URI"));
        MilvusWrapper milvus = MilvusWrapper.getInstance(
                System.getenv("MILVUS_HOST"),
                Integer.parseInt(System.getenv("MILVUS_PORT"))
        );

        KeywordsExtractor keywordsExtractor = KeywordsExtractor.builder().pipeline(Pipeline.getPipeline()).build();
        DocumentSplitter documentSplitter = new DocumentSplitter();
        EmbeddingsExtractor embeddingsExtractor = EmbeddingsExtractor.getInstance("/code/projects/semantica/model/model.onnx");


        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(System.getenv("RABBITMQ_HOST"));
        factory.setUsername(System.getenv("RABBITMQ_USERNAME"));
        factory.setPassword(System.getenv("RABBITMQ_PASSWORD"));

        try (Connection connection = factory.newConnection(); Channel channel = connection.createChannel()) {
            channel.queueDeclare(QUEUE_NAME, true, false, false, null);
            Consumer consumer = new RequestConsumer(channel, mongo, milvus, keywordsExtractor, documentSplitter, embeddingsExtractor);
            channel.basicConsume(QUEUE_NAME, true, consumer);
            System.out.println("Connected");

            // Keep the consumer running until interrupted
            Thread.sleep(Long.MAX_VALUE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
