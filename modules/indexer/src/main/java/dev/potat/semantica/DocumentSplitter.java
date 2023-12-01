package dev.potat.semantica;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.openai.OpenAiTokenizer;

import java.util.List;

import static dev.langchain4j.model.openai.OpenAiModelName.GPT_3_5_TURBO;

public class DocumentSplitter {

    private final dev.langchain4j.data.document.DocumentSplitter splitter;

    public DocumentSplitter(int maxSegmentSize, int maxOverlapSize) {
        this.splitter = DocumentSplitters.recursive(
                maxSegmentSize,
                maxOverlapSize,
                new OpenAiTokenizer(GPT_3_5_TURBO)
        );
    }

    public List<TextSegment> split(String text) {
        Document doc = Document.from(text);
        return this.splitter.split(doc);
    }

    public static class Builder {

        private int maxSegmentSize;
        private int maxOverlapSize;

        public Builder() {
            this.maxSegmentSize = 128;
            this.maxOverlapSize = 16;
        }

        public Builder maxSegmentSize(int maxSegmentSize) {
            this.maxSegmentSize = maxSegmentSize;
            return this;
        }

        public Builder maxOverlapSize(int maxOverlapSize) {
            this.maxOverlapSize = maxOverlapSize;
            return this;
        }

        public DocumentSplitter build() {
            return new DocumentSplitter(maxSegmentSize, maxOverlapSize);
        }
    }

    public Builder builder() {
        return new Builder();
    }
}
