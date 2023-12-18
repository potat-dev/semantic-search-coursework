package dev.potat.semantica.common.keywords;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

@ToString
@NoArgsConstructor
public class Keywords {
    private final HashMap<String, KeywordInfo> keywords = new LinkedHashMap<>();

    public Keywords(Keywords keywords) {
        this.keywords.putAll(keywords.keywords);
    }

    private Keywords(HashMap<String, KeywordInfo> keywords) {
        this.keywords.putAll(keywords);
    }

    public void add(String keyword, float weight, String pos, String ner) {
        keywords.put(keyword, new KeywordInfo(keyword, weight, pos, ner));
    }

    public void add(String keyword, KeywordInfo info) {
        keywords.put(keyword, info);
    }

    public boolean contains(String keyword) {
        return keywords.containsKey(keyword);
    }

    public boolean increment(String keyword) {
        if (!contains(keyword)) return false;
        keywords.get(keyword).increment();
        return true;
    }

    public Set<Map.Entry<String, KeywordInfo>> entrySet() {
        return keywords.entrySet();
    }

//    public Map<String, Float> getSimpleMap() {
//        // extract keywords and their balanced weights from the keywords map
//        return keywords.entrySet().stream().collect(
//                Collectors.toMap(
//                        Map.Entry::getKey,
//                        e -> e.getValue().balancedWeight()
//                )
//        );
//    }

//    public Map<String, Float> getMostImportant(int count) {
//        // get exactly count keywords with the highest balanced weights
//        return getSimpleMap().entrySet().stream()
//                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
//                .limit(count)
//                .collect(Collectors.toMap(
//                        Map.Entry::getKey,
//                        Map.Entry::getValue
//                ));
//    }

    public int size() {
        return keywords.size();
    }

//    public Keywords filter(int maxCount, int minCount, float minWeight) {
    // filtering steps:
    // 1. create new keywords map
    // 2. put exactly maxCount keywords with the highest weights into the new map
    // 3. remove keywords with weight < minWeight but only until the new map size >= minCount
    // 4. return new Keywords object

//        Keywords result = new Keywords();
    // LinkedHashMap<String, KeywordInfo> sorted = entrySet().stream().sorted(
    //         Map.Entry.comparingByValue(
    //                 Comparator.comparing(KeywordInfo::weight)
    //         )
    // ).collect(
    //         LinkedHashMap::new,
    //         (map, entry) -> map.put(entry.getKey(), entry.getValue()),
    //         Map::putAll
    // );
//    }

    public Keywords simpleFilter(float minWeight, int maxCount) {
        HashMap<String, KeywordInfo> collected = keywords.entrySet().stream()
                .filter(entry -> entry.getValue().weight() >= minWeight)
                .sorted(Map.Entry.comparingByValue(Comparator.comparing(KeywordInfo::weight).reversed()))
                .limit(maxCount)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, HashMap::new));

        Keywords result = new Keywords();
        for (Map.Entry<String, KeywordInfo> k : collected.entrySet()) {
                result.add(k.getKey(), k.getValue());
        }
        return result;
    }

    public List<String> getStrKeywords() {
        List<String> result = new ArrayList<>();
        for (Map.Entry<String, KeywordInfo> k : keywords.entrySet()) {
            result.add(k.getKey());
        }
        return result;
    }

    public List<Float> getKeywordsWeights() {
        List<Float> result = new ArrayList<>();
        for (Map.Entry<String, KeywordInfo> k : keywords.entrySet()) {
            result.add(k.getValue().weight());
        }
        return result;
    }

    public record Keyword(String token, float weight) {

    }

    @Getter
    @ToString
    @RequiredArgsConstructor
    public static class KeywordInfo {
        private final String token;
        private int count = 1;
        private final float tokenWeight;
        private final String pos;
        private final String ner;

        public void increment() {
            count++;
        }

        public float weight() {
            return tokenWeight * (float) Math.log(count);
        }

        public Keyword toKeyword() {
            return new Keyword(token, weight());
        }
    }
}
