package dev.potat.semantica.search;

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

    private static final MongoWrapper mongoWrapper = MongoWrapper.getInstance(System.getenv("MONGODB_URI"));
    private static final MilvusWrapper milvusWrapper = MilvusWrapper.getInstance(
            System.getenv("MILVUS_HOST"),
            Integer.parseInt(System.getenv("MILVUS_PORT"))
    );

    public static void main(String[] args) {
        get("/keywords/get", (request, response) -> {
            String query = request.queryParams("q");
            List<String> keywords = keywordsExtractor.extractKeywords(query).simpleFilter(0, 24).getStrKeywords();
            return keywords.toString();
        });

        get("/embeddings/get", (request, response) -> {
            String query = request.queryParams("q");
            List<Float> embeddings = embeddingsExtractor.extract(query);
            return embeddings.toString();
        });

        get("/keywords/search", (request, response) -> {
            String query = request.queryParams("q");
            Keywords keywords = keywordsExtractor.extractKeywords(query).simpleFilter(0, 24);
            return mongoWrapper.searchByKeywords(
                    keywords.getStrKeywords(),
                    keywords.getKeywordsWeights()
            );
        });

        get("/embeddings/search", (request, response) -> {
            String query = request.queryParams("q");
            List<Float> embeddings = embeddingsExtractor.extract(query);
            return milvusWrapper.search(embeddings);
        });

        get("/search", (request, response) -> {
            String query = request.queryParams("q");
            Keywords keywords = keywordsExtractor.extractKeywords(query).simpleFilter(0, 24);
            List<Float> embeddings = embeddingsExtractor.extract(query);

            List<SearchResult> keywordsResults = mongoWrapper.searchByKeywords(
                    keywords.getStrKeywords(),
                    keywords.getKeywordsWeights()
            );
            List<SearchResult> embeddingResults = milvusWrapper.search(embeddings);

            return SearchResultsCombiner.combine(
                    Arrays.asList(keywordsResults, embeddingResults),
                    Arrays.asList(1.2f, 1.0f)
            );
        });
    }
}