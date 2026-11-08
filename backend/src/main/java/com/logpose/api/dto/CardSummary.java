package com.logpose.api.dto;

import com.logpose.api.model.Card;
import com.logpose.api.model.Printing;

import java.util.List;

/**
 * A card as shown in search results. Image, set and rarity come from the card's primary printing.
 */
public record CardSummary(
        String cardNumber,
        String name,
        String category,
        List<String> colors,
        Integer cost,
        Integer power,
        Integer counter,
        String rarity,
        String setCode,
        String imageUrl,
        int printingCount
) {

    public static CardSummary from(Card card) {
        Printing primary = card.getPrimaryPrinting();
        return new CardSummary(
                card.getCardNumber(),
                card.getName(),
                card.getCategory(),
                List.copyOf(card.getColors()),
                card.getCost(),
                card.getPower(),
                card.getCounter(),
                primary == null ? null : primary.getRarity(),
                primary == null ? null : primary.getSetCode(),
                primary == null ? null : primary.getImageUrl(),
                card.getPrintings().size());
    }
}
