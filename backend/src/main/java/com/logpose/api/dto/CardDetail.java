package com.logpose.api.dto;

import com.logpose.api.model.Card;

import java.util.List;

/**
 * Full gameplay data for one card plus all of its printings.
 */
public record CardDetail(
        String cardNumber,
        String name,
        String category,
        List<String> colors,
        List<String> attributes,
        List<String> traits,
        Integer cost,
        Integer power,
        Integer counter,
        String effectText,
        String triggerText,
        List<PrintingDto> printings
) {

    public static CardDetail from(Card card) {
        return new CardDetail(
                card.getCardNumber(),
                card.getName(),
                card.getCategory(),
                List.copyOf(card.getColors()),
                List.copyOf(card.getAttributes()),
                List.copyOf(card.getTraits()),
                card.getCost(),
                card.getPower(),
                card.getCounter(),
                card.getEffectText(),
                card.getTriggerText(),
                card.getPrintings().stream().map(PrintingDto::from).toList());
    }
}
