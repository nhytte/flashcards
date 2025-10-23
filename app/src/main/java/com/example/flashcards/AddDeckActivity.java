package com.example.flashcards; // Upewnij się, że to nazwa Twojego pakietu

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView; // Poprawiony import
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
    private List<Deck> libraryDeckList = new ArrayList<>(); // Lista do wyświetlania (filtrowana)
    private List<Deck> originalLibraryList = new ArrayList<>(); // Pełna lista z biblioteki

    // Klucz API Supabase (zapamiętany)
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
        // Ukryj ikony logowania/profilu
        ImageView iconGoogleLogin = findViewById(R.id.icon_google_login);
        ImageView iconProfile = findViewById(R.id.icon_profile);
        if (iconGoogleLogin != null) iconGoogleLogin.setVisibility(View.GONE);
        if (iconProfile != null) iconProfile.setVisibility(View.GONE);
        // --- Koniec Paska Narzędzi ---

        // Znajdź widoki
        recyclerViewLibrary = findViewById(R.id.recyclerViewLibrary);
        Button buttonCreate = findViewById(R.id.buttonCreate);
        SearchView searchView = findViewById(R.id.searchView); // Znajdź SearchView

        // Ustaw kliknięcie dla PRZYCISKU "Stwórz"
        buttonCreate.setOnClickListener(v -> {
            Intent intent = new Intent(AddDeckActivity.this, CreateDeckActivity.class);
            startActivity(intent);
        });

        // --- Logika Wyszukiwania ---
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                filterDecks(query);
                searchView.clearFocus(); // Schowaj klawiaturę po zatwierdzeniu
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterDecks(newText);
                return true;
            }
        });
        // --- Koniec Logiki Wyszukiwania ---

        // Przygotuj RecyclerView
        setupRecyclerView();
        loadLibraryDecks(); // Załaduj przykładowe dane
    }

    private void setupRecyclerView() {
        LibraryDeckAdapter.OnDeckAddListener listener = deck -> {
            GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
            if (account == null) {
                Toast.makeText(this, "Musisz być zalogowany, aby dodać talię.", Toast.LENGTH_SHORT).show();
                return;
            }
            String userId = account.getId();
            int deckId = deck.getDbId();

            Log.d("AddDeckActivity", "Próba połączenia: UserID = " + userId + ", DeckID = " + deckId);
            if (deckId <= 0) {
                Log.e("AddDeckActivity", "Błąd: Nieprawidłowe DeckID!");
                Toast.makeText(this, "Błąd: Brak ID talii.", Toast.LENGTH_SHORT).show();
                return;
            }
            linkDeckToUser(deckId, userId);
        };

        // Użyj libraryDeckList (listy filtrowanej) dla adaptera
        adapter = new LibraryDeckAdapter(libraryDeckList, listener);
        recyclerViewLibrary.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewLibrary.setAdapter(adapter);
    }

    // Metoda filtrująca listę talii
    private void filterDecks(String query) {
        libraryDeckList.clear(); // Wyczyść aktualnie wyświetlaną listę

        if (query == null || query.isEmpty()) {
            // Jeśli zapytanie jest puste, pokaż wszystkie oryginalne talie
            libraryDeckList.addAll(originalLibraryList);
        } else {
            // Filtruj oryginalną listę na podstawie zapytania (ignoruj wielkość liter)
            String lowerCaseQuery = query.toLowerCase().trim();
            for (Deck deck : originalLibraryList) {
                // Sprawdź, czy tytuł lub opis zawiera zapytanie
                boolean titleMatch = deck.getTitle().toLowerCase().contains(lowerCaseQuery);
                boolean descriptionMatch = deck.getDescription() != null && deck.getDescription().toLowerCase().contains(lowerCaseQuery);
                if (titleMatch || descriptionMatch) {
                    libraryDeckList.add(deck);
                }
            }
        }

        // Powiadom adapter o zmianie danych
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
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
                if (response.isSuccessful() || response.code() == 409) { // 409 Conflict (już istnieje) też jest OK
                    Log.i("AddDeckActivity", "Połączono/Znaleziono talię ID: " + deckId + " z użytkownikiem ID: " + userId);
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

    // Funkcja ładująca zestawy do biblioteki
    private void loadLibraryDecks() {
        originalLibraryList.clear(); // Wyczyść obie listy
        libraryDeckList.clear();

        // Użyj RZECZYWISTYCH ID z tabeli 'decks' w Supabase

        // --- Stare talie ---
        List<Flashcard> mathCards = new ArrayList<>();
        mathCards.add(new Flashcard("a² + b² = ?", "c²"));
        Deck mathDeck = new Deck("Matematyka", "Wzory algebry i geometrii", mathCards);
        mathDeck.setDbId(1); // Zakładane ID
        originalLibraryList.add(mathDeck);

        List<Flashcard> geographyCards = new ArrayList<>();
        geographyCards.add(new Flashcard("Stolica Francji", "Paryż"));
        Deck geoDeck = new Deck("Geografia", "Stolice i góry", geographyCards);
        geoDeck.setDbId(2); // Zakładane ID
        originalLibraryList.add(geoDeck);

        // --- NOWE TALIE Z NOWYMI ID ---
        Deck historyDeck = new Deck("Historia Polski", "Ważne daty i wydarzenia", new ArrayList<>());
        historyDeck.setDbId(100);
        originalLibraryList.add(historyDeck);

        Deck biologyDeck = new Deck("Podstawy Biologii", "Komórki, genetyka i ewolucja", new ArrayList<>());
        biologyDeck.setDbId(101);
        originalLibraryList.add(biologyDeck);

        Deck englishDeck = new Deck("Angielski - Słówka B1", "Przydatne słownictwo na poziomie B1", new ArrayList<>());
        englishDeck.setDbId(102);
        originalLibraryList.add(englishDeck);

        Deck programmingDeck = new Deck("Podstawy Programowania", "Kluczowe koncepcje i definicje", new ArrayList<>());
        programmingDeck.setDbId(103);
        originalLibraryList.add(programmingDeck);

        Deck idiomsDeck = new Deck("Polskie Idiomy", "Popularne zwroty i ich znaczenie", new ArrayList<>());
        idiomsDeck.setDbId(104);
        originalLibraryList.add(idiomsDeck);

        // Na starcie pokaż wszystkie
        libraryDeckList.addAll(originalLibraryList);

        // Powiadom adapter o zmianach
        if (adapter != null) adapter.notifyDataSetChanged();
    }
}