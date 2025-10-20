package com.example.flashcards; // <-- ZMIEŃ NA NAZWĘ SWOJEGO PAKIETU

public class Flashcard {
    private String front;
    private String back;

    public Flashcard(String front, String back) {
        this.front = front;
        this.back = back;
    }

    public String getFront() { return front; }
    public String getBack() { return back; }
}