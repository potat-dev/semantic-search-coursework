package dev.potat.semantica.common.keywords;

import edu.stanford.nlp.pipeline.DefaultPaths;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;

import java.util.Properties;

public class Pipeline {
    private static StanfordCoreNLP pipeline;

    public static StanfordCoreNLP getPipeline() {
        if (pipeline == null) {
            Properties props = new Properties();
            props.setProperty("annotators", "tokenize,pos,lemma,ner");
            props.setProperty("ner.applyFineGrained", "false");
            props.setProperty("ner.model",
                    DefaultPaths.DEFAULT_NER_THREECLASS_MODEL + ',' + DefaultPaths.DEFAULT_NER_CONLL_MODEL);

            pipeline = new StanfordCoreNLP(props);
        }
        return pipeline;
    }
}