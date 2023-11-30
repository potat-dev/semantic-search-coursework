package dev.potat.semantica;

import lombok.Data;

/**
 * A task to be executed by the crawler.
 * Should contain start URL, crawl depth, etc.
 */
@Data
public class CrawlTask {
    private String rootUrl;
    private String textSelector;
    private String linkSelector = "a[href]";
    private Integer maxDepth;
    private Integer maxPages = null;
    private boolean crawlRoot = false; // If false, the root URL will not be crawled (only its children)
}
