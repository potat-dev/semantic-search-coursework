package dev.potat;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

@ToString
@NoArgsConstructor
public class Keywords {
    private static final double INV_E = 1.0 / Math.E;

    private final LinkedHashMap<String, KeywordInfo> keywords = new LinkedHashMap<>();

    public void add(String keyword, float weight, String pos, String ner) {
        keywords.put(keyword, new KeywordInfo(weight, pos, ner));
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

    public int size() {
        return keywords.size();
    }

    @Getter
    @ToString
    @RequiredArgsConstructor
    public static class KeywordInfo {
        private int count = 1;
        private final float weight;
        private final String pos;
        private final String ner;

        public void increment() {
            count++;
        }

        @ToString.Include
        public float balancedWeight() {
            return weight * (float) Math.pow(count, INV_E);
        }
    }
}
