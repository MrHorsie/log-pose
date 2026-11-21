package com.logpose.api.search;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CardSpecificationsTest {

    @Test
    void normalizesSetCodes() {
        assertThat(CardSpecifications.normalizeSet("op01")).isEqualTo("OP01");
        assertThat(CardSpecifications.normalizeSet("OP-01")).isEqualTo("OP01");
        assertThat(CardSpecifications.normalizeSet("op14-eb04")).isEqualTo("OP14EB04");
    }

    @Test
    void normalizesRarities() {
        assertThat(CardSpecifications.normalizeRarity("SR")).isEqualTo("superrare");
        assertThat(CardSpecifications.normalizeRarity("super rare")).isEqualTo("superrare");
        assertThat(CardSpecifications.normalizeRarity("SecretRare")).isEqualTo("secretrare");
        assertThat(CardSpecifications.normalizeRarity("sec")).isEqualTo("secretrare");
        assertThat(CardSpecifications.normalizeRarity("leader")).isEqualTo("leader");
    }
}
