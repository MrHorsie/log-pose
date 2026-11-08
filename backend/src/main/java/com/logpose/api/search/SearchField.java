package com.logpose.api.search;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Searchable fields and the keywords that select them, e.g. {@code color:} or {@code c:}.
 */
public enum SearchField {
    NAME(Kind.TEXT, "name", "n"),
    COLOR(Kind.EXACT, "color", "c"),
    TYPE(Kind.EXACT, "type", "t"),
    COST(Kind.NUMBER, "cost"),
    POWER(Kind.NUMBER, "power", "pow"),
    COUNTER(Kind.NUMBER, "counter"),
    TEXT(Kind.TEXT, "text", "o"),
    TRIGGER(Kind.TEXT, "trigger"),
    TRAIT(Kind.TEXT, "trait", "tr"),
    ATTRIBUTE(Kind.EXACT, "attribute", "attr", "a"),
    SET(Kind.EXACT, "set", "s"),
    RARITY(Kind.EXACT, "rarity", "r"),
    BLOCK(Kind.NUMBER, "block", "b"),
    HAS(Kind.EXACT, "has");

    /** How values are compared: substring, whole value, or number. */
    public enum Kind {TEXT, EXACT, NUMBER}

    private final Kind kind;
    private final List<String> keywords;

    SearchField(Kind kind, String... keywords) {
        this.kind = kind;
        this.keywords = List.of(keywords);
    }

    public Kind kind() {
        return kind;
    }

    public String keyword() {
        return keywords.get(0);
    }

    public static Optional<SearchField> fromKeyword(String keyword) {
        String k = keyword.toLowerCase();
        return Arrays.stream(values()).filter(f -> f.keywords.contains(k)).findFirst();
    }
}
