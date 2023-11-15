package dev.potat;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentType;
import dev.langchain4j.data.document.UrlDocumentLoader;
import dev.langchain4j.data.document.transformer.HtmlTextExtractor;
import edu.stanford.nlp.pipeline.AnnotationPipeline;
import edu.stanford.nlp.pipeline.DefaultPaths;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

public class KeywordsExtractorExample {
    public static void main(String[] args) {
        // String url = "https://en.wikipedia.org/wiki/Word_embedding";
        String url = "https://huggingface.co/blog/getting-started-with-embeddings";
        // String url = "https://www.reddit.com/r/golang/comments/3sfjho/comment/cwxozq9/";

        HtmlTextExtractor textExtractor = new HtmlTextExtractor();
        Document document = textExtractor.transform(UrlDocumentLoader.load(url, DocumentType.HTML));

        StanfordCoreNLP pipeline = Pipeline.getPipeline();
        KeywordsExtractor extractor = KeywordsExtractor.builder()
                .pipeline(pipeline)
                .build();

        // System.out.println(keywords);
        Keywords keywords = extractor.extractKeywords(document.text());
        for (Map.Entry<String, Keywords.KeywordInfo> keyword : keywords.entrySet()) {
            System.out.println(keyword.getKey() + "\t " + keyword.getValue().balancedWeight());
        }
        System.out.println("Count: " + keywords.size());

        Map<String, Float> mostImportant = keywords.getMostImportant(32);



        System.out.println();
        System.out.println("Most important:");
        for (Map.Entry<String, Float> keyword : mostImportant.entrySet()) {
            System.out.println(keyword.getKey() + "\t " + keyword.getValue());
        }

        HashMap<String, String> test = new HashMap<>();
        test.put("foo", "bar");
        test.values().stream().toList();
    }
}
