package com.example.flashcards; // <-- ZMIEŃ NA NAZWĘ SWOJEGO PAKIETU

import java.util.List;

public class Deck {
    private int dbId;

    private String title;
    private String description;
    private List<Flashcard> cards;

    public Deck(String title, String description, List<Flashcard> cards) {
        this.title = title;
        this.description = description;
        this.cards = cards;
    }

    public void setDbId(int dbId) { this.dbId = dbId; }
    public int getDbId() { return dbId; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public List<Flashcard> getCards() { return cards; }
    public int getCardCount() { return cards != null ? cards.size() : 0; }
}