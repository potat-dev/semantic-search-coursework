package dev.potat.semantica.common.embeddings;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class EmbeddingsExtractorTest {
    // this test assumes usage of "labse-en-ru" model in onnx format
    private final static String MODEL_PATH = "/code/projects/semantica/model/model.onnx";
    private final EmbeddingsExtractor extractor = EmbeddingsExtractor.getInstance(MODEL_PATH);

    @Test
    public void testEmbeddingsDimension() {
        String text = "java is the best programming language";
        List<Float> embedding = extractor.extract(text);

        assertFalse(embedding.isEmpty());
        assertEquals(embedding.size(), 768);
    }

    @Test
    public void testGetSampleEmbeddings() {
        String text = "java is the best programming language";
        List<Float> embedding = extractor.extract(text);
        assertFalse(embedding.isEmpty());
        assertAll(
                () -> assertEquals(embedding.get(0), 0.026747204f),
                () -> assertEquals(embedding.get(1), -0.00941038f),
                () -> assertEquals(embedding.get(2), 0.029060591f),
                () -> assertEquals(embedding.get(765), 0.0045365063f),
                () -> assertEquals(embedding.get(766), -0.033344965f),
                () -> assertEquals(embedding.get(767), -0.039263163f)
        );
    }

    @Test
    public void testTextSimilarity() {
        String text = "Java is the best programming language";
        String text1 = "London is the capital of the Great Britain";
        String text2 = "We study Java, it's the best language!";

        List<Float> embedding = extractor.extract(text);
        List<Float> embedding1 = extractor.extract(text1);
        List<Float> embedding2 = extractor.extract(text2);

        double similarityTextAndText1 = EmbeddingsExtractor.cosineSimilarity(embedding, embedding1);
        double similarityTextAndText2 = EmbeddingsExtractor.cosineSimilarity(embedding, embedding2);

        assertTrue(similarityTextAndText1 < similarityTextAndText2);
    }
}