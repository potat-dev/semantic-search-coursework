package dev.potat.semantica;

import lombok.Data;

/**
 * A task to be executed by the crawler.
 * Should contain start URL, crawl depth, etc.
 */
@Data
public class CrawlTask {
    private String startUrl;
    private Integer depth;
    private Integer maxPages = null;
}
