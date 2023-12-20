package dev.potat.semantica.common.keywords;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class KeywordsExtractorTest {
    private final KeywordsExtractor extractor = KeywordsExtractor.builder().build();
    private final String lorem = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Fusce sed rutrum lorem, nec efficitur lacus. " +
            "Curabitur posuere nulla a purus imperdiet, vitae tincidunt ligula commodo. Cras molestie, leo ac feugiat euismod, nulla erat " +
            "lacinia lorem, sed porta elit risus et tortor. Cras lacinia auctor sollicitudin. Nunc accumsan turpis id justo consequat, quis " +
            "dapibus turpis auctor. Donec tempor consequat ipsum, vitae feugiat metus tristique non. Nunc tincidunt lacinia nulla vitae rutrum. " +
            "Curabitur ac est at mi blandit rhoncus eget sit amet sapien. Suspendisse fringilla sem quis sollicitudin dictum. Suspendisse et " +
            "accumsan neque, quis porttitor mauris. Etiam faucibus, dolor a tempus mattis, leo ipsum facilisis nisl, eget pellentesque dolor " +
            "turpis consequat diam.";

    @Test
    public void testEmptyStringToKeywords() {
        String text = "";
        Keywords keywords = extractor.extractKeywords(text);
        assertTrue(keywords.getStrKeywords().isEmpty());
        assertTrue(keywords.getKeywordsWeights().isEmpty());
    }

    @Test
    public void testSampleStringToKeywords() {
        List<String> keywords = extractor.extractKeywords(lorem).getStrKeywords();
        assertTrue(keywords.containsAll(Arrays.asList("lorem", "ipsum", "dolor", "lacus", "purus")));
        assertTrue(keywords.containsAll(Arrays.asList("lorem", "ipsum", "dolor", "lacus", "purus")));
    }

    @Test
    public void testKeywordsFiltering() {
        Keywords keywords = extractor.extractKeywords(lorem).simpleFilter(1.0f, 16);
        List<String> keywordsList = keywords.getStrKeywords();
        List<Float> weightsList = keywords.getKeywordsWeights();

        assertTrue(keywordsList.size() <= 16);
        assertTrue(Collections.min(weightsList) >= 1.0f);
        assertFalse(keywordsList.contains("purus"));  // was filtered
    }
}