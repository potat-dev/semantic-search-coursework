package dev.potat.semantica.common.dataclasses;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SearchResult {
    String url;
    Float score;
}
