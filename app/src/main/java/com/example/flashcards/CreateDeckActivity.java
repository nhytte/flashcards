package com.example.flashcards; // <-- ZMIEŃ NA NAZWĘ SWOJEGO PAKIETU

import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.textfield.TextInputEditText;
import java.util.ArrayList;
import java.util.List;

public class CreateDeckActivity extends AppCompatActivity {

    private TextInputEditText titleEditText, descriptionEditText, cardFrontEditText, cardBackEditText;
    private Button addCardButton, saveDeckButton;
    private RecyclerView addedCardsRecyclerView;

    private List<Flashcard> newCardsList = new ArrayList<>();
    private EditorCardAdapter editorCardAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_deck);

        titleEditText = findViewById(R.id.editTextTitle);
        descriptionEditText = findViewById(R.id.editTextDescription);
        cardFrontEditText = findViewById(R.id.editTextCardFront);
        cardBackEditText = findViewById(R.id.editTextCardBack);
        addCardButton = findViewById(R.id.buttonAddCard);
        saveDeckButton = findViewById(R.id.buttonSaveDeck);
        addedCardsRecyclerView = findViewById(R.id.recyclerViewAddedCards);

        setupRecyclerView();

        addCardButton.setOnClickListener(v -> addCard());
        saveDeckButton.setOnClickListener(v -> saveDeck());
    }

    private void setupRecyclerView() {
        editorCardAdapter = new EditorCardAdapter(newCardsList);
        addedCardsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        addedCardsRecyclerView.setAdapter(editorCardAdapter);
    }

    private void addCard() {
        String front = cardFrontEditText.getText().toString().trim();
        String back = cardBackEditText.getText().toString().trim();

        if (front.isEmpty() || back.isEmpty()) {
            Toast.makeText(this, "Pola fiszki nie mogą być puste!", Toast.LENGTH_SHORT).show();
            return;
        }

        newCardsList.add(new Flashcard(front, back));
        editorCardAdapter.notifyItemInserted(newCardsList.size() - 1);

        cardFrontEditText.setText("");
        cardBackEditText.setText("");
        cardFrontEditText.requestFocus();
    }

    private void saveDeck() {
        String title = titleEditText.getText().toString().trim();
        String description = descriptionEditText.getText().toString().trim();

        if (title.isEmpty()) {
            Toast.makeText(this, "Tytuł nie może być pusty!", Toast.LENGTH_SHORT).show();
            return;
        }
        if (newCardsList.isEmpty()) {
            Toast.makeText(this, "Dodaj przynajmniej jedną fiszkę!", Toast.LENGTH_SHORT).show();
            return;
        }

        Deck newDeck = new Deck(title, description, new ArrayList<>(newCardsList)); // Tworzymy kopię listy
        MainActivity.deckList.add(newDeck);

        Toast.makeText(this, "Zestaw zapisany!", Toast.LENGTH_SHORT).show();
        finish(); // Zamyka aktywność i wraca do MainActivity
    }
}