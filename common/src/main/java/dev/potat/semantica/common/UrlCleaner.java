package dev.potat.semantica.common;

import java.net.URI;
import java.net.URISyntaxException;

public class UrlCleaner {
    public static String clean(String url) throws URISyntaxException {
        URI uri = new URI(url);
        URI cleanedURI = new URI(uri.getScheme(), uri.getAuthority(), uri.getPath(), null, null);
        return cleanedURI.toString();
    }
}
