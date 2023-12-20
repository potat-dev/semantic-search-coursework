package dev.potat.semantica.search;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Consumer;
import dev.potat.semantica.common.dataclasses.CrawlRequest;
import dev.potat.semantica.common.dataclasses.SearchResult;
import dev.potat.semantica.common.embeddings.MilvusWrapper;
import dev.potat.semantica.common.keywords.Keywords;
import dev.potat.semantica.common.keywords.MongoWrapper;
import dev.potat.semantica.common.embeddings.EmbeddingsExtractor;
import dev.potat.semantica.common.keywords.KeywordsExtractor;

import java.util.Arrays;
import java.util.List;

import static spark.Spark.*;


public class SearchService {
    private static final KeywordsExtractor keywordsExtractor = KeywordsExtractor.builder().build();
    private static final EmbeddingsExtractor embeddingsExtractor = EmbeddingsExtractor.getInstance("/code/projects/semantica/model/model.onnx");

    private static final ConnectionFactory rabbit = new ConnectionFactory();
    public static final String QUEUE_NAME = "CRAWL_TASKS";
    private static final MongoWrapper mongo = MongoWrapper.getInstance(System.getenv("MONGODB_URI"));
    private static final MilvusWrapper milvus = MilvusWrapper.getInstance(
            System.getenv("MILVUS_HOST"),
            Integer.parseInt(System.getenv("MILVUS_PORT"))
    );

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static void main(String[] args) {
        rabbit.setHost(System.getenv("RABBITMQ_HOST"));
        rabbit.setUsername(System.getenv("RABBITMQ_USERNAME"));
        rabbit.setPassword(System.getenv("RABBITMQ_PASSWORD"));


        get("/keywords/get", (request, response) -> {
            String query = request.queryParams("q");
            List<String> keywords = keywordsExtractor.extractKeywords(query).simpleFilter(0, 24).getStrKeywords();

            response.type("application/json");
            return objectMapper.writeValueAsString(keywords);
        });

        get("/embeddings/get", (request, response) -> {
            String query = request.queryParams("q");
            List<Float> embeddings = embeddingsExtractor.extract(query);

            response.type("application/json");
            return objectMapper.writeValueAsString(embeddings);
        });

        get("/keywords/search", (request, response) -> {
            String query = request.queryParams("q");
            Keywords keywords = keywordsExtractor.extractKeywords(query).simpleFilter(0, 24);
            List<SearchResult> results = mongo.searchByKeywords(
                    keywords.getStrKeywords(),
                    keywords.getKeywordsWeights()
            );

            response.type("application/json");
            return objectMapper.writeValueAsString(results);
        });

        get("/embeddings/search", (request, response) -> {
            String query = request.queryParams("q");
            List<Float> embeddings = embeddingsExtractor.extract(query);
            List<SearchResult> results = milvus.search(embeddings);

            response.type("application/json");
            return objectMapper.writeValueAsString(results);
        });

        get("/search", (request, response) -> {
            String query = request.queryParams("q");
            Keywords keywords = keywordsExtractor.extractKeywords(query).simpleFilter(0, 24);
            List<Float> embeddings = embeddingsExtractor.extract(query);

            List<SearchResult> keywordsResults = mongo.searchByKeywords(
                    keywords.getStrKeywords(),
                    keywords.getKeywordsWeights()
            );
            List<SearchResult> embeddingResults = milvus.search(embeddings);

            List<SearchResult> combined = SearchResultsCombiner.combine(
                    Arrays.asList(keywordsResults, embeddingResults),
                    Arrays.asList(1.2f, 1.0f)
            );

            response.type("application/json");
            return objectMapper.writeValueAsString(combined);
        });

        post("/index", (request, response) -> {
            response.type("application/json");

            try (Connection connection = rabbit.newConnection(); Channel channel = connection.createChannel()) {
                CrawlRequest indexingRequest = CrawlRequest.builder()
                        .url(request.queryParams("url"))
                        .textSelector(request.queryParams("text_selector"))
                        .maxDepth(Integer.valueOf(request.queryParams("max_depth")))
                        .build();
                channel.basicPublish("", QUEUE_NAME, null,
                        objectMapper.writeValueAsBytes(indexingRequest));
            } catch (Exception e) {
                return "{\"sent\": false}";
            }

            System.out.println("Sent Indexing request");
            return "{\"sent\": true}";
        });
    }
}