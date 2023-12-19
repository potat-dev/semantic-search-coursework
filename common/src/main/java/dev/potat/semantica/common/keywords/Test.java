package dev.potat.semantica.common.keywords;

public class Test {
    public static void main(String[] args) {
        KeywordsExtractor extractor = KeywordsExtractor.builder()
                .pipeline(Pipeline.getPipeline())
                .build();
        Keywords keywords = extractor.extractKeywords(
//                "Overview In this quick tutorial, we'll look at the different ways of iterating through the entries of a Map in Java."
                "java async library"
        ).simpleFilter(0, 24);
        System.out.println(keywords);

        System.out.println(keywords.getStrKeywords());
        System.out.println(keywords.getKeywordsWeights());
    }
}
