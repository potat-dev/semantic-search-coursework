package dev.potat;

import edu.stanford.nlp.ling.*;
import edu.stanford.nlp.pipeline.*;
import edu.stanford.nlp.util.Timing;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.RequiredArgsConstructor;

import java.util.*;

@Builder
@AllArgsConstructor
@RequiredArgsConstructor
public class KeywordsExtractor {
    private static final double TOKENS_REMOVE_WINDOW_FACTOR = 1.2;

    // configs
    @Builder.Default
    private final float minWeight = 2.0f;

    @Builder.Default
    private final int maxCount = 32;

    @Builder.Default
    private final boolean removeFromTail = false;

    @Builder.Default
    private final Map<String, Float> nerTagsWeights = Map.of(
            "PERSON", 1.0f,
            "LOCATION", 1.0f,
            "ORGANIZATION", 1.0f,
            "MISC", 1.0f
    );

    @Builder.Default
    private final Map<String, Float> posPrefixesWeights = Map.of(
            "NN", 1.0f,
            "JJ", 1.0f
    );

    // pipeline
    StanfordCoreNLP pipeline;

    public Keywords extractKeywords(String text) {
        System.out.println("Extracting keywords from text...");
        Keywords keywords = new Keywords();

        // TODO: add ability to init Keywords with a list of starting keywords
        // they will be determined from the "helper string" (e.g. title)

        Timing timer = new Timing();

        CoreDocument document = pipeline.processToCoreDocument(text);
        for (CoreLabel tok : document.tokens()) {
            String keyword = tok.lemma();
            if (!keywords.increment(keyword)) {
                float weight = calculateTokenWeight(tok);
                if (weight == 0.0f) continue;
                keywords.add(keyword, weight, tok.tag(), tok.ner());
            }
        }

        System.out.println("Time elapsed: " + timer.toSecondsString());

        // TODO: rewrite this to use the new Keywords class
        // maybe move to Keywords class

        // LinkedHashMap<String, Float> result = new LinkedHashMap<>();
        // for (Map.Entry<String, Keywords.KeywordInfo> entry : keywords.entrySet()) {
        //     String keyword = entry.getKey();
        //     float weight = entry.getValue().balancedWeight();
        //     if (weight >= minWeight) {
        //         result.put(keyword, weight);
        //     }
        // }

        // tokensToRemove = len(keywords) - maxCount
        // get (tokensToRemove * 1.2) at the end of the list
        // and remove the tokensToRemove tokens with the lowest weight

        // int tokensToRemove = result.size() - maxCount;
        // int tokensRemoveWindow = (int) (tokensToRemove * TOKENS_REMOVE_WINDOW_FACTOR);
        // List<Map.Entry<String, Float>> entries = new ArrayList<>(result.entrySet());
        // if (removeFromTail) {
        //     entries = entries.subList(Math.max(result.size() - tokensRemoveWindow, 0), result.size());
        // }
        //
        // entries.sort(Map.Entry.comparingByValue());
        // for (int i = 0; i < tokensToRemove; i++) {
        //     result.remove(entries.get(i).getKey());
        // }

        // return result;
        return keywords;
    }

    private float calculateTokenWeight(CoreLabel tok) {
        float weight = 0.0f;
        String nerTag = tok.ner();
        if (nerTagsWeights.containsKey(nerTag)) weight += nerTagsWeights.get(nerTag);
        for (Map.Entry<String, Float> entry : posPrefixesWeights.entrySet()) {
            String prefix = entry.getKey();
            float w = entry.getValue();
            if (tok.tag().startsWith(prefix)) {
                weight += w;
                break;
            }
        }
        return weight;
    }
}
