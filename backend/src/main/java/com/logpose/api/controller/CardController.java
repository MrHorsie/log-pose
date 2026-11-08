package com.logpose.api.controller;

import com.logpose.api.dto.CardDetail;
import com.logpose.api.dto.CardSummary;
import com.logpose.api.dto.PageResponse;
import com.logpose.api.service.CardService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/cards")
public class CardController {

    private final CardService cardService;

    public CardController(CardService cardService) {
        this.cardService = cardService;
    }

    /**
     * Search cards, e.g. {@code GET /api/cards?q=c:red t:character cost<=3}.
     */
    @GetMapping
    public PageResponse<CardSummary> search(
            @RequestParam(defaultValue = "") String q,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "60") @Min(1) @Max(120) int size,
            @RequestParam(defaultValue = "name") String order,
            @RequestParam(defaultValue = "asc") String dir) {
        return cardService.search(q, page, size, order, dir);
    }

    @GetMapping("/{cardNumber}")
    public CardDetail getCard(@PathVariable String cardNumber) {
        return cardService.getCard(cardNumber)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No card " + cardNumber));
    }
}
