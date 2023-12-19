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
}
