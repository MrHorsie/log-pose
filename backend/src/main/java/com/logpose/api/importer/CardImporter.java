package com.logpose.api.importer;

import com.logpose.api.model.Card;
import com.logpose.api.model.Printing;
import com.logpose.api.repository.CardRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Imports optcg-data into the database. Safe to re-run: cards are matched by card number and
 * printings by source ID, so existing rows are updated instead of duplicated.
 */
@Service
public class CardImporter {

    private static final Logger log = LoggerFactory.getLogger(CardImporter.class);

    private final CardRepository cardRepository;
    private final CardDataReader reader;
    private final JsonMapper jsonMapper;

    public CardImporter(CardRepository cardRepository, CardDataReader reader, JsonMapper jsonMapper) {
        this.cardRepository = cardRepository;
        this.reader = reader;
        this.jsonMapper = jsonMapper;
    }

    public record ImportResult(int cardsCreated, int cardsUpdated, int printingsCreated, int printingsUpdated) {
    }

    @Transactional
    public ImportResult importFrom(String source) {
        log.info("Importing card data from {}", source);
        List<SourcePack> packs = jsonMapper.readValue(reader.read(source, "packs.json"),
                new TypeReference<List<SourcePack>>() {
                });
        Map<String, String> setNames = packs.stream()
                .collect(Collectors.toMap(SourcePack::code, SourcePack::name, (a, b) -> a));

        // Keyed by source ID so duplicate entries in the data (there is at least one) collapse to one printing
        Map<String, SourceCard> entries = new LinkedHashMap<>();
        for (SourcePack pack : packs) {
            List<SourceCard> cards = jsonMapper.readValue(reader.read(source, "cards/" + pack.code() + ".json"),
                    new TypeReference<List<SourceCard>>() {
                    });
            for (SourceCard sc : cards) {
                entries.put(sc.id(), sc);
            }
        }
        log.info("Read {} printings from {} sets", entries.size(), packs.size());

        Map<String, List<SourceCard>> byCardNumber = entries.values().stream()
                .collect(Collectors.groupingBy(SourceCard::baseNumber, LinkedHashMap::new, Collectors.toList()));

        Map<String, Card> existing = cardRepository.findAll().stream()
                .collect(Collectors.toMap(Card::getCardNumber, Function.identity()));

        int cardsCreated = 0, cardsUpdated = 0, printingsCreated = 0, printingsUpdated = 0;
        List<Card> toSave = new ArrayList<>();

        for (var group : byCardNumber.entrySet()) {
            Card card = existing.get(group.getKey());
            if (card == null) {
                card = new Card();
                card.setCardNumber(group.getKey());
                cardsCreated++;
            } else {
                cardsUpdated++;
            }
            applyGameplayFields(card, canonicalEntry(group.getValue()));

            Map<String, Printing> printingsBySourceId = card.getPrintings().stream()
                    .collect(Collectors.toMap(Printing::getSourceId, Function.identity()));
            for (SourceCard sc : group.getValue()) {
                Printing printing = printingsBySourceId.get(sc.id());
                if (printing == null) {
                    printing = new Printing();
                    printing.setSourceId(sc.id());
                    card.addPrinting(printing);
                    printingsCreated++;
                } else {
                    printingsUpdated++;
                }
                printing.setSetCode(sc.set());
                printing.setSetName(setNames.get(sc.set()));
                printing.setRarity(sc.rarity());
                printing.setImageUrl(sc.image());
                printing.setBlockNumber(sc.block());
            }
            toSave.add(card);
        }

        cardRepository.saveAll(toSave);
        ImportResult result = new ImportResult(cardsCreated, cardsUpdated, printingsCreated, printingsUpdated);
        log.info("Import finished: {}", result);
        return result;
    }

    /**
     * Gameplay data comes from the base printing when present. Reprints occasionally differ in
     * whitespace or errata, and the base entry is the most consistent choice.
     */
    static SourceCard canonicalEntry(List<SourceCard> group) {
        return group.stream().filter(SourceCard::isBasePrinting).findFirst().orElse(group.get(0));
    }

    private static void applyGameplayFields(Card card, SourceCard sc) {
        card.setName(sc.name());
        card.setCategory(sc.category());
        card.setCost(sc.cost());
        card.setPower(sc.power());
        card.setCounter(sc.counter());
        card.setEffectText(blankToNull(sc.effect()));
        card.setTriggerText(blankToNull(sc.trigger()));
        replace(card.getColors(), sc.colors());
        replace(card.getAttributes(), sc.attributes());
        replace(card.getTraits(), sc.types());
    }

    private static void replace(List<String> target, List<String> values) {
        target.clear();
        if (values != null) {
            target.addAll(values);
        }
    }

    private static String blankToNull(String s) {
        return s == null || s.isBlank() ? null : s;
    }
}
