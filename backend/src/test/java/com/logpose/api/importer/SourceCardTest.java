package com.logpose.api.importer;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SourceCardTest {

    private static SourceCard card(String id, String set) {
        return new SourceCard(id, set, "Thousand Sunny", "Common", "Stage", List.of("Black"), 1, null, null,
                2, List.of(), List.of("Straw Hat Crew"), "effect", null, "image");
    }

    @Test
    void suffixedIdsShareTheBaseNumber() {
        assertThat(card("ST14-017", "ST-14").baseNumber()).isEqualTo("ST14-017");
        assertThat(card("ST14-017_p1", "PRB-02").baseNumber()).isEqualTo("ST14-017");
        assertThat(card("ST14-017_r1", "PRB-02").baseNumber()).isEqualTo("ST14-017");
    }

    @Test
    void canonicalEntryPrefersBasePrinting() {
        SourceCard altArt = card("ST14-017_p1", "PRB-02");
        SourceCard base = card("ST14-017", "ST-14");

        assertThat(CardImporter.canonicalEntry(List.of(altArt, base))).isSameAs(base);
    }

    @Test
    void canonicalEntryFallsBackToFirstWhenNoBasePrinting() {
        SourceCard first = card("ST14-017_p1", "PRB-02");
        SourceCard second = card("ST14-017_r1", "PRB-02");

        assertThat(CardImporter.canonicalEntry(List.of(first, second))).isSameAs(first);
    }
}
