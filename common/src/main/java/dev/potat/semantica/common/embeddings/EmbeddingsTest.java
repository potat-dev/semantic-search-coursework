package dev.potat.semantica.common.embeddings;

import java.util.List;

public class EmbeddingsTest {
    public static void main(String[] args) {
        EmbeddingsExtractor extractor = EmbeddingsExtractor.getInstance("/code/projects/semantica/model/model.onnx");
        List<Float> embedding = extractor.extract("java");
        System.out.println(embedding);
        System.out.println(embedding.size());
    }
}
