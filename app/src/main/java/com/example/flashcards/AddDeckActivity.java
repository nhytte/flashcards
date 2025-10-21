package com.example.flashcards; // Upewnij się, że to nazwa Twojego pakietu

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button; // <-- DODAJ TEN IMPORT
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
// import com.google.android.material.card.MaterialCardView; // Można usunąć
import java.util.ArrayList;
import java.util.List;

public class AddDeckActivity extends AppCompatActivity {

    // private MaterialCardView cardCreateOwn; // Można usunąć
    private RecyclerView recyclerViewLibrary;
    private LibraryDeckAdapter adapter;
    private List<Deck> libraryDeckList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_deck);

        // --- Ustawienie Paska Narzędzi (Toolbar) ---
        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
        ImageView backArrow = findViewById(R.id.toolbar_back_arrow);
        backArrow.setVisibility(View.VISIBLE);
        backArrow.setOnClickListener(v -> onBackPressed());
        // --- Koniec Paska Narzędzi ---

        // Znajdź widoki
        // cardCreateOwn = findViewById(R.id.cardCreateOwn); // Można usunąć
        recyclerViewLibrary = findViewById(R.id.recyclerViewLibrary);
        Button buttonCreate = findViewById(R.id.buttonCreate); // Znajdujemy przycisk wewnątrz karty

        // Ustaw kliknięcie dla PRZYCISKU "Stwórz"
        buttonCreate.setOnClickListener(v -> {
            Intent intent = new Intent(AddDeckActivity.this, CreateDeckActivity.class);
            startActivity(intent);
        });

        // Przygotuj RecyclerView
        setupRecyclerView();
        loadLibraryDecks(); // Załaduj przykładowe dane
    }

    private void setupRecyclerView() {
        LibraryDeckAdapter.OnDeckAddListener listener = deck -> {
            // Logika dodawania zestawu do listy głównej
            MainActivity.deckList.add(deck);
            Toast.makeText(this, "Dodano zestaw: " + deck.getTitle(), Toast.LENGTH_SHORT).show();
            // Wróć do ekranu głównego
            finish();
        };

        adapter = new LibraryDeckAdapter(libraryDeckList, listener);
        recyclerViewLibrary.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewLibrary.setAdapter(adapter);
    }

    // Funkcja ładująca przykładowe zestawy do biblioteki
    private void loadLibraryDecks() {
        List<Flashcard> mathCards = new ArrayList<>();
        mathCards.add(new Flashcard("a² + b² = ?", "c²"));
        mathCards.add(new Flashcard("sin(90°)", "1"));
        mathCards.add(new Flashcard("Pole koła", "πr²"));
        libraryDeckList.add(new Deck("Matematyka", "Wzory algebry i geometrii", mathCards));

        List<Flashcard> geographyCards = new ArrayList<>();
        geographyCards.add(new Flashcard("Stolica Francji", "Paryż"));
        geographyCards.add(new Flashcard("Najwyższy szczyt świata", "Mount Everest"));
        libraryDeckList.add(new Deck("Geografia", "Stolice i góry", geographyCards));

        if (adapter != null) { // Dodano sprawdzenie, czy adapter istnieje
            adapter.notifyDataSetChanged();
        }
    }
}