package com.example.flashcards; // Upewnij się, że to nazwa Twojego pakietu

import android.content.Intent;
import android.os.Bundle;
import android.util.Log; // <-- DODAJ TEN IMPORT
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddDeckActivity extends AppCompatActivity {

    private RecyclerView recyclerViewLibrary;
    private LibraryDeckAdapter adapter;
    private List<Deck> libraryDeckList = new ArrayList<>();

    // !!! ZASTĄP SWOIM KLUCZEM !!!
    private static final String SUPABASE_API_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6IndmaGRuYmdmdXFjaG11bnBtaGV6Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3NjA5NjA0MzEsImV4cCI6MjA3NjUzNjQzMX0.fx6xY49V2hUG0msmo1pvGp9bnkDzKrBMs4iZT7_t8Zo";

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
        recyclerViewLibrary = findViewById(R.id.recyclerViewLibrary);
        Button buttonCreate = findViewById(R.id.buttonCreate);

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
            // Sprawdź, czy użytkownik jest zalogowany
            GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
            if (account == null) {
                Toast.makeText(this, "Musisz być zalogowany, aby dodać talię.", Toast.LENGTH_SHORT).show();
                return;
            }
            String userId = account.getId();
            int deckId = deck.getDbId();

            // --- DODANO LOGOWANIE DANYCH ---
            Log.d("AddDeckActivity", "Próba połączenia: UserID = " + userId + ", DeckID = " + deckId);
            if (deckId <= 0) {
                Log.e("AddDeckActivity", "Błąd: Nieprawidłowe DeckID!");
                Toast.makeText(this, "Błąd: Brak ID talii.", Toast.LENGTH_SHORT).show();
                return;
            }
            // --- KONIEC LOGOWANIA ---

            // Wyślij żądanie do API, aby połączyć talię z użytkownikiem
            linkDeckToUser(deckId, userId);
        };

        adapter = new LibraryDeckAdapter(libraryDeckList, listener);
        recyclerViewLibrary.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewLibrary.setAdapter(adapter);
    }

    // Metoda pomocnicza do linkowania talii
    private void linkDeckToUser(long deckId, String userId) {
        ApiService apiService = RetrofitClient.getApiService();
        String authorizationHeader = "Bearer " + SUPABASE_API_KEY;
        UserDeckLink link = new UserDeckLink(userId, deckId);

        Call<Void> linkCall = apiService.saveUserDeck(link, SUPABASE_API_KEY, authorizationHeader);
        linkCall.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Log.i("AddDeckActivity", "Połączono talię ID: " + deckId + " z użytkownikiem ID: " + userId);
                    Toast.makeText(AddDeckActivity.this, "Dodano zestaw!", Toast.LENGTH_SHORT).show();
                    finish(); // Sukces - zamknij aktywność
                } else {
                    Log.e("AddDeckActivity", "Błąd łączenia talii z biblioteki: " + response.code() + " - " + response.message());
                    try {
                        Log.e("AddDeckActivity", "Error body: " + (response.errorBody() != null ? response.errorBody().string() : "null"));
                    } catch (Exception e) { e.printStackTrace(); }
                    Toast.makeText(AddDeckActivity.this, "Błąd zapisu.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.e("AddDeckActivity", "Błąd sieci przy łączeniu talii", t);
                Toast.makeText(AddDeckActivity.this, "Błąd sieci.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Funkcja ładująca przykładowe zestawy do biblioteki
    private void loadLibraryDecks() {
        // Użyj RZECZYWISTYCH ID z tabeli 'decks' w Supabase
        List<Flashcard> mathCards = new ArrayList<>();
        mathCards.add(new Flashcard("a² + b² = ?", "c²"));
        Deck mathDeck = new Deck("Matematyka", "Wzory...", mathCards);
        mathDeck.setDbId(2); // <-- ZASTĄP RZECZYWISTYM ID
        libraryDeckList.add(mathDeck);

        List<Flashcard> geographyCards = new ArrayList<>();
        geographyCards.add(new Flashcard("Stolica Francji", "Paryż"));
        Deck geoDeck = new Deck("Geografia", "Stolice...", geographyCards);
        geoDeck.setDbId(1); // <-- ZASTĄP RZECZYWISTYM ID
        libraryDeckList.add(geoDeck);

        if (adapter != null) adapter.notifyDataSetChanged();
    }
}