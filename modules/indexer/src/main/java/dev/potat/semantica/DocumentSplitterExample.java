package dev.potat.semantica;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentType;
import dev.langchain4j.data.document.UrlDocumentLoader;
import dev.langchain4j.data.document.transformer.HtmlTextExtractor;
import dev.langchain4j.data.segment.TextSegment;

import java.util.ArrayList;

public class DocumentSplitterExample {
    public static <List> void main(String[] args) {
        String url = "https://huggingface.co/blog/getting-started-with-embeddings";

        HtmlTextExtractor textExtractor = new HtmlTextExtractor();
        Document document = textExtractor.transform(UrlDocumentLoader.load(url, DocumentType.HTML));

        DocumentSplitter splitter = new DocumentSplitter(128, 16);
        ArrayList<TextSegment> segments = (ArrayList<TextSegment>) splitter.split(document.text());

        for (TextSegment segment : segments) {
            System.out.println(segment.text());
            System.out.println("--------------");
        }

        System.out.println("Count: " + segments.size());
    }
}
