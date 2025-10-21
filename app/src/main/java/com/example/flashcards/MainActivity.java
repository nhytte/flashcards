package com.example.flashcards; // Upewnij się, że to nazwa Twojego pakietu

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;


import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private DeckAdapter deckAdapter;
    public static List<Deck> deckList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // --- POCZĄTEK KODU DLA SPLASH SCREEN ---
        // Ta linia MUSI być przed super.onCreate() i setContentView()
        // --- KONIEC KODU DLA SPLASH SCREEN ---

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // --- POCZĄTEK KODU DLA TOOLBARA ---
        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Wyłączamy domyślny tytuł, bo mamy własny w XML
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
        // (Nasz niestandardowy przycisk 'toolbar_back_arrow' jest już ukryty)
        // --- KONIEC KODU DLA TOOLBARA ---

        if (deckList.isEmpty()) {
            addSampleData();
        }

        recyclerView = findViewById(R.id.recyclerViewDecks);
        FloatingActionButton fab = findViewById(R.id.fabAddDeck);

        setupRecyclerView();

        fab.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, CreateDeckActivity.class);
            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (deckAdapter != null) {
            deckAdapter.notifyDataSetChanged();
        }
    }

    private void setupRecyclerView() {
        DeckAdapter.OnDeckClickListener listener = new DeckAdapter.OnDeckClickListener() {
            @Override
            public void onPracticeClick(int position) {
                Intent intent = new Intent(MainActivity.this, LearningActivity.class);
                intent.putExtra("DECK_INDEX", position);
                startActivity(intent);
            }

            @Override
            public void onDeleteClick(int position) {
                if (position >= 0 && position < deckList.size()) {
                    deckList.remove(position);
                    deckAdapter.notifyItemRemoved(position);
                    Toast.makeText(MainActivity.this, "Usunięto zestaw", Toast.LENGTH_SHORT).show();
                }
            }
        };

        deckAdapter = new DeckAdapter(deckList, this, listener);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(deckAdapter);
    }

    private void addSampleData() {
        List<Flashcard> spanishCards = new ArrayList<>();
        spanishCards.add(new Flashcard("Hola", "Cześć"));
        spanishCards.add(new Flashcard("Adiós", "Żegnaj"));
        deckList.add(new Deck("Hiszpański A1", "Podstawowe zwroty", spanishCards));

        List<Flashcard> mathCards = new ArrayList<>();
        mathCards.add(new Flashcard("a² + b² = ?", "c²"));
        deckList.add(new Deck("Wzory matematyczne", "Geometria", mathCards));
    }
}