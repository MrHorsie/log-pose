package com.logpose.api.search;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class QueryParserTest {

    @Test
    void blankQueryMatchesEverything() {
        assertThat(QueryParser.parse("")).isNull();
        assertThat(QueryParser.parse("   ")).isNull();
        assertThat(QueryParser.parse(null)).isNull();
    }

    @Test
    void parsesSingleFilter() {
        assertThat(QueryParser.parse("c:red"))
                .isEqualTo(new QueryNode.Term(SearchField.COLOR, Operator.COLON, "red"));
    }

    @Test
    void aliasesAndKeywordsAreCaseInsensitive() {
        assertThat(QueryParser.parse("COLOR:Red"))
                .isEqualTo(new QueryNode.Term(SearchField.COLOR, Operator.COLON, "Red"));
        assertThat(QueryParser.parse("o:draw"))
                .isEqualTo(new QueryNode.Term(SearchField.TEXT, Operator.COLON, "draw"));
    }

    @Test
    void parsesAdjacentTermsAsAnd() {
        QueryNode node = QueryParser.parse("c:red t:character cost<=3 counter:2000 o:\"DON!!\"");

        assertThat(node).isEqualTo(new QueryNode.And(List.of(
                new QueryNode.Term(SearchField.COLOR, Operator.COLON, "red"),
                new QueryNode.Term(SearchField.TYPE, Operator.COLON, "character"),
                new QueryNode.Term(SearchField.COST, Operator.LTE, "3"),
                new QueryNode.Term(SearchField.COUNTER, Operator.COLON, "2000"),
                new QueryNode.Term(SearchField.TEXT, Operator.COLON, "DON!!"))));
    }

    @Test
    void parsesTriggerHasAndBlockFields() {
        assertThat(QueryParser.parse("has:trigger"))
                .isEqualTo(new QueryNode.Term(SearchField.HAS, Operator.COLON, "trigger"));
        assertThat(QueryParser.parse("trigger:draw"))
                .isEqualTo(new QueryNode.Term(SearchField.TRIGGER, Operator.COLON, "draw"));
        assertThat(QueryParser.parse("block>=3"))
                .isEqualTo(new QueryNode.Term(SearchField.BLOCK, Operator.GTE, "3"));
        assertThat(QueryParser.parse("b:2"))
                .isEqualTo(new QueryNode.Term(SearchField.BLOCK, Operator.COLON, "2"));
    }

    @Test
    void cntIsNotAnAliasForCounter() {
        assertThatThrownBy(() -> QueryParser.parse("cnt:1000"))
                .hasMessage("Unknown search field 'cnt'");
    }

    @Test
    void parsesAllComparisonOperators() {
        assertThat(QueryParser.parse("cost=1")).isEqualTo(new QueryNode.Term(SearchField.COST, Operator.EQ, "1"));
        assertThat(QueryParser.parse("cost!=1")).isEqualTo(new QueryNode.Term(SearchField.COST, Operator.NEQ, "1"));
        assertThat(QueryParser.parse("cost<1")).isEqualTo(new QueryNode.Term(SearchField.COST, Operator.LT, "1"));
        assertThat(QueryParser.parse("cost<=1")).isEqualTo(new QueryNode.Term(SearchField.COST, Operator.LTE, "1"));
        assertThat(QueryParser.parse("cost>1")).isEqualTo(new QueryNode.Term(SearchField.COST, Operator.GT, "1"));
        assertThat(QueryParser.parse("cost>=1")).isEqualTo(new QueryNode.Term(SearchField.COST, Operator.GTE, "1"));
    }

    @Test
    void orBindsLooserThanAnd() {
        QueryNode node = QueryParser.parse("c:red t:leader or c:blue");

        assertThat(node).isEqualTo(new QueryNode.Or(List.of(
                new QueryNode.And(List.of(
                        new QueryNode.Term(SearchField.COLOR, Operator.COLON, "red"),
                        new QueryNode.Term(SearchField.TYPE, Operator.COLON, "leader"))),
                new QueryNode.Term(SearchField.COLOR, Operator.COLON, "blue"))));
    }

    @Test
    void parenthesesGroup() {
        QueryNode node = QueryParser.parse("(c:red OR c:green) t:leader");

        assertThat(node).isEqualTo(new QueryNode.And(List.of(
                new QueryNode.Or(List.of(
                        new QueryNode.Term(SearchField.COLOR, Operator.COLON, "red"),
                        new QueryNode.Term(SearchField.COLOR, Operator.COLON, "green"))),
                new QueryNode.Term(SearchField.TYPE, Operator.COLON, "leader"))));
    }

    @Test
    void dashNegates() {
        assertThat(QueryParser.parse("-t:event"))
                .isEqualTo(new QueryNode.Not(new QueryNode.Term(SearchField.TYPE, Operator.COLON, "event")));
        assertThat(QueryParser.parse("-(c:red)"))
                .isEqualTo(new QueryNode.Not(new QueryNode.Term(SearchField.COLOR, Operator.COLON, "red")));
    }

    @Test
    void bareWordsAndPhrasesSearchNames() {
        assertThat(QueryParser.parse("luffy"))
                .isEqualTo(new QueryNode.Term(null, Operator.COLON, "luffy"));
        assertThat(QueryParser.parse("\"Monkey.D.Luffy\""))
                .isEqualTo(new QueryNode.Term(null, Operator.COLON, "Monkey.D.Luffy"));
    }

    @Test
    void bareWordsMayContainPunctuation() {
        assertThat(QueryParser.parse("OP01-001"))
                .isEqualTo(new QueryNode.Term(null, Operator.COLON, "OP01-001"));
        assertThat(QueryParser.parse("DON!!"))
                .isEqualTo(new QueryNode.Term(null, Operator.COLON, "DON!!"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"foo:bar", "(c:red", "c:red )", "or c:red", "c:red or", "\"open", "c:", "()"})
    void rejectsInvalidQueries(String query) {
        assertThatThrownBy(() -> QueryParser.parse(query)).isInstanceOf(SearchQueryException.class);
    }

    @Test
    void unknownFieldMessageNamesTheField() {
        assertThatThrownBy(() -> QueryParser.parse("foo:bar"))
                .hasMessage("Unknown search field 'foo'");
    }
}
