package dev.potat.semantica.crawler;

import dev.potat.semantica.common.CrawlRequest;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;

public class Crawler {
    private final String userAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/119.0.0.0 Safari/537.36 Edg/119.0.0.0";

    public Crawler() {

    }

    public void crawl(CrawlRequest request) throws IOException {
        Document doc = Jsoup.connect(url).userAgent(userAgent).get();
    }
}
