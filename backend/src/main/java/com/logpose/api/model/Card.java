package com.logpose.api.model;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "cards")

public class Card {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


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

    private String category;
    private String name;
    private Integer cost;
    private Integer power;
    private Integer counter;
    private String rarity;
    @Column(columnDefinition = "TEXT")
    private String effectText;



    @OneToMany(mappedBy = "card", cascade = CascadeType.ALL)
    private List<Printing> printings = new ArrayList<>();


    public Card() {

    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }


    public String getEffectText() {
        return effectText;
    }

    public void setEffectText(String effectText) {
        this.effectText = effectText;
    }

    public String getRarity() {
        return rarity;
    }

    public void setRarity(String rarity) {
        this.rarity = rarity;
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



}
