package dev.potat.semantica.common.dataclasses;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CrawlRequest {
    private String url;
    private String textSelector;
    private String linkSelector = "a[href]";

    private Integer maxDepth;
    private Integer currentDepth = 0;
}
