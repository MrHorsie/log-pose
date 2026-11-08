package com.logpose.api.service;

import com.logpose.api.dto.CardDetail;
import com.logpose.api.dto.CardSummary;
import com.logpose.api.dto.PageResponse;
import com.logpose.api.repository.CardRepository;
import com.logpose.api.search.CardSpecifications;
import com.logpose.api.search.QueryParser;
import com.logpose.api.search.SearchQueryException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Optional;

@Service
public class CardService {

    // Public sort names mapped to entity properties
    private static final Map<String, String> SORT_PROPERTIES = Map.of(
            "name", "name",
            "number", "cardNumber",
            "cost", "cost",
            "power", "power",
            "counter", "counter");

    private final CardRepository cardRepository;

    public CardService(CardRepository cardRepository) {
        this.cardRepository = cardRepository;
    }

    @Transactional(readOnly = true)
    public PageResponse<CardSummary> search(String query, int page, int size, String order, String direction) {
        String property = SORT_PROPERTIES.get(order.toLowerCase());
        if (property == null) {
            throw new SearchQueryException("Unknown sort order '" + order + "'; use one of " + SORT_PROPERTIES.keySet());
        }
        Sort.Direction dir = "desc".equalsIgnoreCase(direction) ? Sort.Direction.DESC : Sort.Direction.ASC;
        // Card number as a tiebreaker keeps paging stable when many cards share a cost or name
        Sort sort = Sort.by(dir, property).and(Sort.by("cardNumber"));

        var spec = CardSpecifications.from(QueryParser.parse(query));
        return PageResponse.from(cardRepository.findAll(spec, PageRequest.of(page, size, sort))
                .map(CardSummary::from));
    }

    @Transactional(readOnly = true)
    public Optional<CardDetail> getCard(String cardNumber) {
        return cardRepository.findByCardNumberIgnoreCase(cardNumber).map(CardDetail::from);
    }
}
