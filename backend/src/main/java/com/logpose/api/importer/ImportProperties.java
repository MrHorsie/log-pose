package com.logpose.api.importer;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Settings for the card data import.
 *
 * @param enabled run the import on startup
 * @param source  base location of the optcg-data "data" folder: an http(s) URL or a local directory
 */
@ConfigurationProperties(prefix = "logpose.import")
public record ImportProperties(boolean enabled, String source) {

    // Our fork of michalkiral/optcg-data (archived August 2026), which keeps scraping new sets weekly.
    // Pinned to a commit so imports are reproducible; bump it to pick up new sets.
    public static final String DEFAULT_SOURCE =
            "https://cdn.jsdelivr.net/gh/MrHorsie/optcg-data@da45c4e520297c5a237316ff45030a0eb74d6db7/data";

    public ImportProperties {
        if (source == null || source.isBlank()) {
            source = DEFAULT_SOURCE;
        }
    }
}
