package dev.potat.semantica.search;

import dev.potat.semantica.common.MongoWrapper;
import dev.potat.semantica.common.embeddings.EmbeddingsExtractor;
import dev.potat.semantica.common.keywords.KeywordsExtractor;

import java.util.List;

import static spark.Spark.*;


public class SearchService {
    private static final KeywordsExtractor keywordsExtractor = KeywordsExtractor.builder().build();
    private static final EmbeddingsExtractor embeddingsExtractor = EmbeddingsExtractor.getInstance("/code/projects/semantica/model/model.onnx");

    private static MongoWrapper mongoWrapper = MongoWrapper.getInstance(System.getenv("MONGODB_URI"));

    public static void main(String[] args) {
        get("/keywords", (request, response) -> {
            String query = request.queryParams("q");
            List<String> keywords = keywordsExtractor.extractKeywords(query).simpleFilter(0, 24).getStrKeywords();
            return keywords.toString();
        });

        get("/embeddings", (request, response) -> {
            String query = request.queryParams("q");
            List<Float> embeddings = embeddingsExtractor.extract(query);
            return embeddings.toString();
        });

        get("/search", (request, response) -> {
            String query = request.queryParams("q");
            List<String> keywords = keywordsExtractor.extractKeywords(query).simpleFilter(0, 24).getStrKeywords();
            List<Float> embeddings = embeddingsExtractor.extract(query);


            return query;
        });
    }
}