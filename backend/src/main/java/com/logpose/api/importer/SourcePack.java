package com.logpose.api.importer;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * One entry from optcg-data's data/packs.json.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record SourcePack(String code, String name) {
}
