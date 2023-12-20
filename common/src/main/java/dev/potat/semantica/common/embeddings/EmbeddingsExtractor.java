package dev.potat.semantica.common.embeddings;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.embedding.OnnxEmbeddingModel;
import lombok.Synchronized;

import java.util.List;

public class EmbeddingsExtractor {
    private static EmbeddingsExtractor instance;
    EmbeddingModel embeddingModel;

    public EmbeddingsExtractor(String modelPath) {
        embeddingModel = new OnnxEmbeddingModel(modelPath);
    }

    public static EmbeddingsExtractor getInstance(String modelPath) {
        if (instance == null) {
            instance = new EmbeddingsExtractor(modelPath);
        }
        return instance;
    }

    @Synchronized
    public List<Float> extract(String text) {
        Embedding e = embeddingModel.embed(text).content();
        return e.vectorAsList();
    }

    @Synchronized
    public List<Float> extract(TextSegment text) {
        Embedding e = embeddingModel.embed(text).content();
        return e.vectorAsList();
    }

    public static double cosineSimilarity(List<Float> vector1, List<Float> vector2) {
        if (vector1.size() != vector2.size() || vector1.isEmpty()) {
            throw new IllegalArgumentException("Input vectors must have the same size and cannot be empty");
        }

        double dotProduct = 0;
        double norm1 = 0;
        double norm2 = 0;

        for (int i = 0; i < vector1.size(); i++) {
            dotProduct += vector1.get(i) * vector2.get(i);
            norm1 += Math.pow(vector1.get(i), 2);
            norm2 += Math.pow(vector2.get(i), 2);
        }

        if (norm1 == 0 || norm2 == 0) {
            return 0; // Cosine similarity is not defined if one of the vectors is a zero vector
        }

        return dotProduct / (Math.sqrt(norm1) * Math.sqrt(norm2));

    }
}
