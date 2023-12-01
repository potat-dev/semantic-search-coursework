package dev.potat.semantica.common;

import lombok.Data;

@Data
public class CrawlRequest {
    private String rootUrl;
    private String textSelector;
    private String linkSelector = "a[href]";
    private Integer maxDepth;
    private Integer maxCount;
    private Boolean crawlRoot = false;
}
