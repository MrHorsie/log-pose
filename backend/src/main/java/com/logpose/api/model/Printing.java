package com.logpose.api.model;


import jakarta.persistence.*;

@Entity
@Table(name = "printings")
public class Printing {

    @Id
    @GeneratedValue
    private Long id;

    private String sourceId;
    private String setCode;
    private String imageUrl;
    private Integer blockNumber;
    private boolean nonRotating;


    @ManyToOne
    @JoinColumn(name = "card_id", nullable = false)
    private Card card;

    public Printing() {

    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Card getCard() {
        return card;
    }

    public void setCard(Card card) {
        this.card = card;
    }

    public boolean isNonRotating() {
        return nonRotating;
    }

    public void setNonRotating(boolean nonRotating) {
        this.nonRotating = nonRotating;
    }

    public Integer getBlockNumber() {
        return blockNumber;
    }

    public void setBlockNumber(Integer blockNumber) {
        this.blockNumber = blockNumber;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getSetCode() {
        return setCode;
    }

    public void setSetCode(String setCode) {
        this.setCode = setCode;
    }

    public String getSourceId() {
        return sourceId;
    }

    public void setSourceId(String sourceId) {
        this.sourceId = sourceId;
    }
}
