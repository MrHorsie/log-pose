package com.logpose.api.search;

import java.util.ArrayList;
import java.util.List;

/**
 * Recursive-descent parser for the search syntax.
 *
 * <pre>
 * query   := orExpr
 * orExpr  := andExpr ("or" andExpr)*
 * andExpr := unary+                      (terms next to each other are ANDed)
 * unary   := "-" unary | primary
 * primary := "(" orExpr ")" | term
 * term    := word | "quoted phrase" | keyword op (word | "quoted phrase")
 * op      := ":" | "=" | "!=" | "&lt;" | "&lt;=" | "&gt;" | "&gt;="
 * </pre>
 */
public final class QueryParser {

    private enum TokenType {LPAREN, RPAREN, NOT, OR, TERM}

    private record Token(TokenType type, QueryNode.Term term) {
    }

    private final List<Token> tokens;
    private int pos;

    private QueryParser(List<Token> tokens) {
        this.tokens = tokens;
    }

    /**
     * Parses a query. Returns null for a blank query, meaning "match everything".
     */
    public static QueryNode parse(String query) {
        if (query == null || query.isBlank()) {
            return null;
        }
        QueryParser parser = new QueryParser(new Lexer(query).tokenize());
        QueryNode node = parser.parseOr();
        if (parser.pos < parser.tokens.size()) {
            throw new SearchQueryException("Unexpected ')'");
        }
        return node;
    }

    private QueryNode parseOr() {
        List<QueryNode> children = new ArrayList<>();
        children.add(parseAnd());
        while (peek(TokenType.OR)) {
            pos++;
            children.add(parseAnd());
        }
        return children.size() == 1 ? children.get(0) : new QueryNode.Or(children);
    }

    private QueryNode parseAnd() {
        List<QueryNode> children = new ArrayList<>();
        while (pos < tokens.size() && !peek(TokenType.OR) && !peek(TokenType.RPAREN)) {
            children.add(parseUnary());
        }
        if (children.isEmpty()) {
            throw new SearchQueryException(pos < tokens.size()
                    ? "Expected a search term before '" + (peek(TokenType.OR) ? "or" : ")") + "'"
                    : "Expected a search term at the end of the query");
        }
        return children.size() == 1 ? children.get(0) : new QueryNode.And(children);
    }

    private QueryNode parseUnary() {
        if (peek(TokenType.NOT)) {
            pos++;
            if (pos >= tokens.size()) {
                throw new SearchQueryException("Expected a search term after '-'");
            }
            return new QueryNode.Not(parseUnary());
        }
        return parsePrimary();
    }

    private QueryNode parsePrimary() {
        Token token = tokens.get(pos);
        if (token.type() == TokenType.LPAREN) {
            pos++;
            QueryNode inner = parseOr();
            if (!peek(TokenType.RPAREN)) {
                throw new SearchQueryException("Missing closing ')'");
            }
            pos++;
            return inner;
        }
        if (token.type() == TokenType.TERM) {
            pos++;
            return token.term();
        }
        throw new SearchQueryException("Unexpected '" + (token.type() == TokenType.OR ? "or" : ")") + "'");
    }

    private boolean peek(TokenType type) {
        return pos < tokens.size() && tokens.get(pos).type() == type;
    }

    /**
     * Splits the raw query string into tokens.
     */
    private static final class Lexer {

        private final String input;
        private int i;

        Lexer(String input) {
            this.input = input;
        }

        List<Token> tokenize() {
            List<Token> out = new ArrayList<>();
            while (true) {
                skipWhitespace();
                if (i >= input.length()) {
                    return out;
                }
                char ch = input.charAt(i);
                if (ch == '(') {
                    i++;
                    out.add(new Token(TokenType.LPAREN, null));
                } else if (ch == ')') {
                    i++;
                    out.add(new Token(TokenType.RPAREN, null));
                } else if (ch == '-' && i + 1 < input.length() && !Character.isWhitespace(input.charAt(i + 1))) {
                    i++;
                    out.add(new Token(TokenType.NOT, null));
                } else if (ch == '"') {
                    out.add(new Token(TokenType.TERM, new QueryNode.Term(null, Operator.COLON, readQuoted())));
                } else {
                    out.add(readWordOrFilter());
                }
            }
        }

        private Token readWordOrFilter() {
            int start = i;
            while (i < input.length() && Character.isLetter(input.charAt(i))) {
                i++;
            }
            String keyword = input.substring(start, i);
            Operator op = keyword.isEmpty() ? null : readOperator();
            if (op == null) {
                i = start;
                String word = readBareValue();
                if (word.equalsIgnoreCase("or")) {
                    return new Token(TokenType.OR, null);
                }
                return new Token(TokenType.TERM, new QueryNode.Term(null, Operator.COLON, word));
            }
            SearchField field = SearchField.fromKeyword(keyword)
                    .orElseThrow(() -> new SearchQueryException("Unknown search field '" + keyword + "'"));
            String value = i < input.length() && input.charAt(i) == '"' ? readQuoted() : readBareValue();
            if (value.isEmpty()) {
                throw new SearchQueryException("Missing value after '" + keyword + op.symbol() + "'");
            }
            return new Token(TokenType.TERM, new QueryNode.Term(field, op, value));
        }

        private Operator readOperator() {
            String rest = input.substring(i);
            for (Operator op : new Operator[]{Operator.NEQ, Operator.LTE, Operator.GTE,
                    Operator.COLON, Operator.EQ, Operator.LT, Operator.GT}) {
                if (rest.startsWith(op.symbol())) {
                    i += op.symbol().length();
                    return op;
                }
            }
            return null;
        }

        private String readBareValue() {
            int start = i;
            while (i < input.length()) {
                char ch = input.charAt(i);
                if (Character.isWhitespace(ch) || ch == '(' || ch == ')') {
                    break;
                }
                i++;
            }
            return input.substring(start, i);
        }

        private String readQuoted() {
            int start = ++i;
            int end = input.indexOf('"', start);
            if (end < 0) {
                throw new SearchQueryException("Missing closing quote");
            }
            i = end + 1;
            return input.substring(start, end);
        }

        private void skipWhitespace() {
            while (i < input.length() && Character.isWhitespace(input.charAt(i))) {
                i++;
            }
        }
    }
}
