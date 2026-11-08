package com.logpose.api.search;

import com.logpose.api.model.Card;
import com.logpose.api.model.Printing;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import org.springframework.data.jpa.domain.Specification;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;

/**
 * Turns a parsed {@link QueryNode} into a JPA {@link Specification} (a SQL WHERE clause).
 *
 * <p>Filters on collections (colors, traits, printings) use EXISTS subqueries rather than joins,
 * so a card with several matching printings still appears only once and paging stays correct.
 */
public final class CardSpecifications {

    // Short forms accepted by rarity:, mapped to the values stored in the data
    private static final Map<String, String> RARITY_ALIASES = Map.ofEntries(
            Map.entry("c", "common"),
            Map.entry("uc", "uncommon"),
            Map.entry("u", "uncommon"),
            Map.entry("r", "rare"),
            Map.entry("sr", "superrare"),
            Map.entry("sec", "secretrare"),
            Map.entry("l", "leader"),
            Map.entry("sp", "special"),
            Map.entry("tr", "treasurerare"),
            Map.entry("p", "promo"));

    // Values accepted by has:, mapped to the Card property that must be present (ordered for error messages)
    private static final Map<String, String> HAS_PROPERTIES = new LinkedHashMap<>();

    static {
        HAS_PROPERTIES.put("trigger", "triggerText");
        HAS_PROPERTIES.put("counter", "counter");
        HAS_PROPERTIES.put("effect", "effectText");
        HAS_PROPERTIES.put("power", "power");
    }

    private CardSpecifications() {
    }

    public static Specification<Card> from(QueryNode node) {
        return (root, query, cb) -> node == null ? cb.conjunction() : toPredicate(node, root, query, cb);
    }

    private static Predicate toPredicate(QueryNode node, Root<Card> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
        return switch (node) {
            case QueryNode.And and -> cb.and(and.children().stream()
                    .map(child -> toPredicate(child, root, query, cb)).toArray(Predicate[]::new));
            case QueryNode.Or or -> cb.or(or.children().stream()
                    .map(child -> toPredicate(child, root, query, cb)).toArray(Predicate[]::new));
            case QueryNode.Not not -> cb.not(toPredicate(not.child(), root, query, cb));
            case QueryNode.Term term -> termPredicate(term, root, query, cb);
        };
    }

    private static Predicate termPredicate(QueryNode.Term term, Root<Card> root, CriteriaQuery<?> query,
                                           CriteriaBuilder cb) {
        if (term.field() == null) {
            // Bare word or phrase: name contains it, or it is the card number
            return cb.or(contains(cb, root.get("name"), term.value()),
                    cb.equal(cb.lower(root.get("cardNumber")), term.value().toLowerCase(Locale.ROOT)));
        }

        SearchField field = term.field();
        Operator op = term.operator();
        if (op.isComparison() && field.kind() != SearchField.Kind.NUMBER) {
            throw new SearchQueryException("'" + field.keyword() + "' does not support " + op.symbol()
                    + "; use ':' instead");
        }

        Predicate predicate = switch (field) {
            case NAME -> textMatch(cb, root.get("name"), term);
            case TEXT -> cb.or(textMatch(cb, root.get("effectText"), term), textMatch(cb, root.get("triggerText"), term));
            case TRIGGER -> textMatch(cb, root.get("triggerText"), term);
            case HAS -> hasPredicate(cb, root, term.value());
            // Block is per printing (reprints can move a card to a newer block)
            case BLOCK -> existsPrinting(root, query, cb, printing -> compare(cb, printing.get("blockNumber"), term));
            case TYPE -> cb.equal(cb.lower(root.get("category")), term.value().toLowerCase(Locale.ROOT));
            case COLOR -> existsInCollection(root, query, cb, "colors",
                    value -> cb.equal(cb.lower(value), term.value().toLowerCase(Locale.ROOT)));
            case ATTRIBUTE -> existsInCollection(root, query, cb, "attributes",
                    value -> cb.equal(cb.lower(value), term.value().toLowerCase(Locale.ROOT)));
            case TRAIT -> existsInCollection(root, query, cb, "traits", value -> textMatch(cb, value, term));
            case SET -> existsPrinting(root, query, cb, printing -> cb.equal(
                    cb.function("replace", String.class, cb.upper(printing.get("setCode")), cb.literal("-"), cb.literal("")),
                    normalizeSet(term.value())));
            case RARITY -> existsPrinting(root, query, cb, printing -> cb.equal(
                    cb.lower(printing.get("rarity")), normalizeRarity(term.value())));
            case COST -> compare(cb, root.get("cost"), term);
            case POWER -> compare(cb, root.get("power"), term);
            // Cards without a counter are treated as counter 0, so counter:0 finds them
            case COUNTER -> compare(cb, cb.coalesce(root.<Integer>get("counter"), 0), term);
        };

        return op == Operator.NEQ ? cb.not(predicate) : predicate;
    }

    /**
     * ':' and '!=' match substrings; '=' matches the whole value. Always case-insensitive.
     */
    private static Predicate textMatch(CriteriaBuilder cb, Expression<String> expression, QueryNode.Term term) {
        if (term.operator() == Operator.EQ) {
            return cb.equal(cb.lower(expression), term.value().toLowerCase(Locale.ROOT));
        }
        return contains(cb, expression, term.value());
    }

    private static Predicate contains(CriteriaBuilder cb, Expression<String> expression, String value) {
        String escaped = value.toLowerCase(Locale.ROOT)
                .replace("\\", "\\\\")
                .replace("%", "\\%")
                .replace("_", "\\_");
        return cb.like(cb.lower(expression), "%" + escaped + "%", '\\');
    }

    private static Predicate compare(CriteriaBuilder cb, Expression<Integer> expression, QueryNode.Term term) {
        int number;
        try {
            number = Integer.parseInt(term.value());
        } catch (NumberFormatException e) {
            throw new SearchQueryException("'" + term.field().keyword() + "' needs a whole number, got '"
                    + term.value() + "'");
        }
        return switch (term.operator()) {
            case COLON, EQ, NEQ -> cb.equal(expression, number);
            case LT -> cb.lessThan(expression, number);
            case LTE -> cb.lessThanOrEqualTo(expression, number);
            case GT -> cb.greaterThan(expression, number);
            case GTE -> cb.greaterThanOrEqualTo(expression, number);
        };
    }

    /** has:trigger, has:counter, has:effect, has:power: the card has a value for that property. */
    private static Predicate hasPredicate(CriteriaBuilder cb, Root<Card> root, String value) {
        String property = HAS_PROPERTIES.get(value.toLowerCase(Locale.ROOT));
        if (property == null) {
            throw new SearchQueryException("Unknown value 'has:" + value + "'; use one of "
                    + String.join(", ", HAS_PROPERTIES.keySet()));
        }
        return cb.isNotNull(root.get(property));
    }

    private static Predicate existsInCollection(Root<Card> root, CriteriaQuery<?> query, CriteriaBuilder cb,
                                                String collection, Function<Expression<String>, Predicate> match) {
        Subquery<Integer> sub = query.subquery(Integer.class);
        Root<Card> inner = sub.from(Card.class);
        Join<Card, String> value = inner.join(collection);
        sub.select(cb.literal(1)).where(cb.equal(inner, root), match.apply(value));
        return cb.exists(sub);
    }

    private static Predicate existsPrinting(Root<Card> root, CriteriaQuery<?> query, CriteriaBuilder cb,
                                            Function<Root<Printing>, Predicate> match) {
        Subquery<Integer> sub = query.subquery(Integer.class);
        Root<Printing> printing = sub.from(Printing.class);
        sub.select(cb.literal(1)).where(cb.equal(printing.get("card"), root), match.apply(printing));
        return cb.exists(sub);
    }

    /** "op01", "OP-01" and "OP01" all match set code "OP-01". */
    static String normalizeSet(String value) {
        return value.toUpperCase(Locale.ROOT).replace("-", "");
    }

    /** "SR", "super rare" and "SuperRare" all match "SuperRare". */
    static String normalizeRarity(String value) {
        String compact = value.toLowerCase(Locale.ROOT).replaceAll("[^a-z]", "");
        return RARITY_ALIASES.getOrDefault(compact, compact);
    }
}
