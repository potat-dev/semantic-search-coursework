package dev.potat.semantica.common.embeddings;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.openai.OpenAiModelName;
import dev.langchain4j.model.openai.OpenAiTokenizer;
import lombok.Builder;
import org.checkerframework.checker.signature.qual.BinaryName;

import java.util.ArrayList;
import java.util.List;

public class DocumentSplitter {
    private final dev.langchain4j.data.document.DocumentSplitter splitter;

    private static final int maxSegmentSize = 64; // in tokens
    private static final int maxOverlapSize = 16; // in tokens
    private static final int minSegmentSize = 32; // in chars

    public DocumentSplitter() {
        this.splitter = DocumentSplitters.recursive(
                maxSegmentSize,
                maxOverlapSize,
                new OpenAiTokenizer(OpenAiModelName.GPT_3_5_TURBO)
        );
    }

    public List<String> split(String text) {
        Document doc = Document.from(text);
        List<String> segments = new ArrayList<>();
        for (TextSegment segment : splitter.split(doc)) {
            if (segment.text().length() >= minSegmentSize) {
                segments.add(segment.text());
            }
        }
        return segments;
    }
}
