package dev.potat;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentType;
import dev.langchain4j.data.document.UrlDocumentLoader;
import dev.langchain4j.data.document.transformer.HtmlTextExtractor;

import java.util.LinkedHashMap;

public class KeywordsExtractorExample {
    public static void main(String[] args) {
        // String url = "https://en.wikipedia.org/wiki/Word_embedding";
        String url = "https://huggingface.co/blog/getting-started-with-embeddings";
        // String url = "https://www.reddit.com/r/golang/comments/3sfjho/comment/cwxozq9/";

        HtmlTextExtractor textExtractor = new HtmlTextExtractor();
        Document document = textExtractor.transform(UrlDocumentLoader.load(url, DocumentType.HTML));

        KeywordsExtractor extractor = KeywordsExtractor.builder()
                .maxCount(32)
                .minWeight(2.0f)
                .build();

        LinkedHashMap<String, Float> keywords = extractor.extract(document.text());

        // System.out.println(keywords);
        System.out.println(keywords.keySet());
        System.out.println("Count: " + keywords.size());
    }
}
