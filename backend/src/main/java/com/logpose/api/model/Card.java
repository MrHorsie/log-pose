package com.logpose.api.model;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

/**
 * A gameplay card: the rules-relevant data shared by every printing of the same card number.
 */
@Entity
@Table(name = "cards")
public class Card {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Base card number from the data source, e.g. "OP01-001" (printings like "OP01-001_p1" group under it)
    @Column(nullable = false, unique = true)
    private String cardNumber;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String category;

    @ElementCollection
    @CollectionTable(name = "card_colors", joinColumns = @JoinColumn(name = "card_id"))
    @Column(name = "color")
    private List<String> colors = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "card_attributes", joinColumns = @JoinColumn(name = "card_id"))
    @Column(name = "attribute")
    private List<String> attributes = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "card_traits", joinColumns = @JoinColumn(name = "card_id"))
    @Column(name = "trait")
    private List<String> traits = new ArrayList<>();

    private Integer cost;
    private Integer power;
    private Integer counter;

    @Column(columnDefinition = "TEXT")
    private String effectText;

    @Column(columnDefinition = "TEXT")
    private String triggerText;

    @OneToMany(mappedBy = "card", cascade = CascadeType.ALL)
    @OrderBy("sourceId")
    private List<Printing> printings = new ArrayList<>();

    public Card() {

    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
    }

    public String getEffectText() {
        return effectText;
    }

    public void setEffectText(String effectText) {
        this.effectText = effectText;
    }

    public String getTriggerText() {
        return triggerText;
    }

    public void setTriggerText(String triggerText) {
        this.triggerText = triggerText;
    }

    public Integer getCounter() {
        return counter;
    }

    public void setCounter(Integer counter) {
        this.counter = counter;
    }

    public Integer getCost() {
        return cost;
    }

    public void setCost(Integer cost) {
        this.cost = cost;
    }

    public Integer getPower() {
        return power;
    }

    public void setPower(Integer power) {
        this.power = power;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getColors() {
        return colors;
    }

    public void setColors(List<String> colors) {
        this.colors = colors;
    }

    public List<String> getAttributes() {
        return attributes;
    }

    public void setAttributes(List<String> attributes) {
        this.attributes = attributes;
    }

    public List<String> getTraits() {
        return traits;
    }

    public void setTraits(List<String> traits) {
        this.traits = traits;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public List<Printing> getPrintings() {
        return printings;
    }

    public void setPrintings(List<Printing> printings) {
        this.printings = printings;
    }

    public void addPrinting(Printing printing) {
        this.printings.add(printing);
        printing.setCard(this);
    }

    /**
     * The printing whose source ID equals the card number (the original release), or the first printing.
     */
    public Printing getPrimaryPrinting() {
        return printings.stream()
                .filter(p -> cardNumber.equals(p.getSourceId()))
                .findFirst()
                .orElse(printings.isEmpty() ? null : printings.get(0));
    }
}
