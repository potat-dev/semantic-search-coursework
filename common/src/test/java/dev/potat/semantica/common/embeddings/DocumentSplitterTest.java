package dev.potat.semantica.common.embeddings;

import org.junit.Before;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class DocumentSplitterTest {
    private final DocumentSplitter splitter = new DocumentSplitter();

    @Test
    public void testSplitEmptyString() {
        String text = "";
        List<String> parts = splitter.split(text);
        assertTrue(parts.isEmpty());
    }

    @Test
    public void testSplitSampleString() {
        String text = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Fusce sed rutrum lorem, nec efficitur lacus. Curabitur posuere nulla a purus imperdiet, vitae tincidunt ligula commodo. Cras molestie, leo ac feugiat euismod, " +
                "nulla erat lacinia lorem, sed porta elit risus et tortor. Cras lacinia auctor sollicitudin. Nunc accumsan turpis id justo consequat, quis dapibus turpis auctor. Donec tempor consequat ipsum, vitae feugiat metus tristique non. " +
                "Nunc tincidunt lacinia nulla vitae rutrum. Curabitur ac est at mi blandit rhoncus eget sit amet sapien. Suspendisse fringilla sem quis sollicitudin dictum. Suspendisse et accumsan neque, quis porttitor mauris. Etiam faucibus, " +
                "dolor a tempus mattis, leo ipsum facilisis nisl, eget pellentesque dolor turpis consequat diam.";

        List<String> correct = Arrays.asList(
                "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Fusce sed rutrum lorem, nec efficitur lacus. Curabitur posuere nulla a purus imperdiet, vitae tincidunt ligula commodo.",
                "Cras molestie, leo ac feugiat euismod, nulla erat lacinia lorem, sed porta elit risus et tortor. Cras lacinia auctor sollicitudin. Nunc accumsan turpis id justo consequat, quis dapibus turpis auctor.",
                "Donec tempor consequat ipsum, vitae feugiat metus tristique non. Nunc tincidunt lacinia nulla vitae rutrum. Curabitur ac est at mi blandit rhoncus eget sit amet sapien. Suspendisse fringilla sem quis sollicitudin dictum.",
                "Suspendisse fringilla sem quis sollicitudin dictum. Suspendisse et accumsan neque, quis porttitor mauris. Etiam faucibus, dolor a tempus mattis, leo ipsum facilisis nisl, eget pellentesque dolor turpis consequat diam."
        );

        List<String> parts = splitter.split(text);
        assertEquals(parts, correct);
    }
}