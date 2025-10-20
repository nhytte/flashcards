package com.example.flashcards; // <-- UPEWNIJ SIĘ, ŻE TO NAZWA TWOJEGO PAKIETU

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
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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

    /**
     * Ta metoda jest już zaktualizowana i zawiera logikę
     * przechodzenia do LearningActivity.
     */
    private void setupRecyclerView() {
        DeckAdapter.OnDeckClickListener listener = new DeckAdapter.OnDeckClickListener() {
            @Override
            public void onPracticeClick(int position) {
                // TUTAJ JEST ZAIMPLEMENTOWANA ZMIANA
                Intent intent = new Intent(MainActivity.this, LearningActivity.class);
                intent.putExtra("DECK_INDEX", position); // Przekazujemy pozycję klikniętego zestawu
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