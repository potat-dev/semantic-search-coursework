package dev.potat.semantica.common.keywords;

import dev.potat.semantica.common.dataclasses.SearchResult;
import dev.potat.semantica.common.keywords.MongoWrapper;

import java.util.Arrays;
import java.util.List;

public class MongoTest {
    public static void main(String[] args) {
        MongoWrapper mongo = MongoWrapper.getInstance("mongodb://192.168.1.40:27017/");
        List<SearchResult> results = mongo.searchByKeywords(
                Arrays.asList("java", "test"),
                Arrays.asList(1.4f, 1.0f)
        );

        for (SearchResult result : results) {
            System.out.println(result);
        }
    }
}
