package com.logpose.api.importer;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

/**
 * One entry from the optcg-data card JSON files (data/cards/SET.json).
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record SourceCard(
        String id,
        String set,
        String name,
        String rarity,
        String category,
        List<String> colors,
        Integer cost,
        Integer power,
        Integer counter,
        Integer block,
        List<String> attributes,
        List<String> types,
        String effect,
        String trigger,
        String image
) {

    /** "OP01-001_p1" -> "OP01-001". Alternate arts and reprints share the base number. */
    public String baseNumber() {
        int underscore = id.indexOf('_');
        return underscore < 0 ? id : id.substring(0, underscore);
    }

    public boolean isBasePrinting() {
        return id.indexOf('_') < 0;
    }
}
