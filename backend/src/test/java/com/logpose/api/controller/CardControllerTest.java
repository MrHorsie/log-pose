package com.logpose.api.controller;

import com.logpose.api.dto.CardSummary;
import com.logpose.api.dto.PageResponse;
import com.logpose.api.search.SearchQueryException;
import com.logpose.api.service.CardService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CardController.class)
class CardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CardService cardService;

    @Test
    void searchReturnsPagedResults() throws Exception {
        CardSummary zoro = new CardSummary("OP01-001", "Roronoa Zoro", "Leader", List.of("Red"),
                5, 5000, null, "Leader", "OP-01", "https://example.com/OP01-001.png", 2);
        given(cardService.search(eq("zoro"), eq(0), eq(60), eq("name"), eq("asc")))
                .willReturn(new PageResponse<>(List.of(zoro), 0, 60, 1, 1));

        mockMvc.perform(get("/api/cards").param("q", "zoro"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalItems").value(1))
                .andExpect(jsonPath("$.items[0].cardNumber").value("OP01-001"));
    }

    @Test
    void invalidQueryReturnsProblemDetail() throws Exception {
        given(cardService.search(anyString(), anyInt(), anyInt(), anyString(), anyString()))
                .willThrow(new SearchQueryException("Unknown search field 'foo'"));

        mockMvc.perform(get("/api/cards").param("q", "foo:bar"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("Unknown search field 'foo'"));
    }

    @Test
    void pageSizeIsLimited() throws Exception {
        mockMvc.perform(get("/api/cards").param("size", "500"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void unknownCardReturns404() throws Exception {
        given(cardService.getCard("NOPE-001")).willReturn(Optional.empty());

        mockMvc.perform(get("/api/cards/NOPE-001"))
                .andExpect(status().isNotFound());
    }
}
