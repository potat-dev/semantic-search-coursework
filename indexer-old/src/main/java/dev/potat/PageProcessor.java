package dev.potat;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentType;
import dev.langchain4j.data.document.UrlDocumentLoader;
import dev.langchain4j.data.document.transformer.HtmlTextExtractor;
import edu.stanford.nlp.pipeline.DefaultPaths;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;

import javax.print.Doc;
import java.util.LinkedHashMap;
import java.util.Properties;

public class PageProcessor {
    private final StanfordCoreNLP pipeline;
    private final HtmlTextExtractor htmlTextExtractor;


    public PageProcessor() {
        Properties pipelineProps = new Properties();
        pipelineProps.setProperty("annotators", "tokenize,pos,lemma,ner");
        pipelineProps.setProperty("ner.applyFineGrained", "false");
        pipelineProps.setProperty("ner.model",
                DefaultPaths.DEFAULT_NER_THREECLASS_MODEL + ',' + DefaultPaths.DEFAULT_NER_CONLL_MODEL);

        this.pipeline = new StanfordCoreNLP(pipelineProps);
        this.htmlTextExtractor = new HtmlTextExtractor();

    }

    public void process(String url) {
        Document document = UrlDocumentLoader.load(url, DocumentType.HTML);
        // document.metadata().add("foo", "bar");
        System.out.println(document.metadata());
        // document = htmlTextExtractor.transform(document);

        // KeywordsExtractor extractor = KeywordsExtractor.builder()
        //         .maxCount(32)
        //         .minWeight(2.0f)
        //         .build();

        // LinkedHashMap<String, Float> keywords = extractor.extract(document.text());
    }


    public static void main(String[] args) {
        String url = "https://huggingface.co/blog/getting-started-with-embeddings";

        PageProcessor pageProcessor = new PageProcessor();
        pageProcessor.process(url);
    }
}
