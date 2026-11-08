package com.logpose.api.search;

import java.util.List;

/**
 * Parsed search query as a tree. {@code c:red (cost<=3 or -t:event)} becomes
 * And[Term(color), Or[Term(cost), Not[Term(type)]]].
 */
public sealed interface QueryNode {

    record And(List<QueryNode> children) implements QueryNode {
    }

    record Or(List<QueryNode> children) implements QueryNode {
    }

    record Not(QueryNode child) implements QueryNode {
    }

    /**
     * A single filter. {@code field} is null for a bare word, which searches names and card numbers.
     */
    record Term(SearchField field, Operator operator, String value) implements QueryNode {
    }
}
