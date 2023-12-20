package dev.potat.semantica.search;

import dev.potat.semantica.common.UrlCleaner;
import dev.potat.semantica.common.dataclasses.SearchResult;

import java.net.URISyntaxException;
import java.util.*;

public class SearchResultsCombiner {
    public static List<SearchResult> combine(List<List<SearchResult>> searchResults, List<Float> priorities) {
        List<SearchResult> output = new ArrayList<>();
        HashMap<String, Float> urls = new HashMap<>();

        int resultsIndex = 0;
        for (List<SearchResult> results : searchResults) {
            for (SearchResult result : results) {
                try {
                    String url = UrlCleaner.clean(result.getUrl());
                    if (urls.containsKey(url)) {
                        urls.put(url, urls.get(url) + result.getScore() * priorities.get(resultsIndex));
                    } else {
                        urls.put(url, result.getScore());
                    }
                } catch (URISyntaxException e) {
                    throw new RuntimeException(e);
                }
            }
            resultsIndex += 1;
        }

        for (Map.Entry<String, Float> url : urls.entrySet()) {
            output.add(
                    SearchResult.builder()
                            .url(url.getKey())
                            .score(url.getValue())
                            .build()
            );
        }

        output.sort(Comparator.comparing(SearchResult::getScore).reversed());
        return output;
    }
}
