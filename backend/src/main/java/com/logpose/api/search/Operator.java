package com.logpose.api.search;

public enum Operator {
    COLON(":"),
    EQ("="),
    NEQ("!="),
    LT("<"),
    LTE("<="),
    GT(">"),
    GTE(">=");

    private final String symbol;

    Operator(String symbol) {
        this.symbol = symbol;
    }

    public String symbol() {
        return symbol;
    }

    public boolean isComparison() {
        return this == LT || this == LTE || this == GT || this == GTE;
    }
}
