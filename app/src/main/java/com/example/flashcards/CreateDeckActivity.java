package com.example.flashcards; // Upewnij się, że to nazwa Twojego pakietu

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.HashMap; // Upewnij się, że ten import jest
import java.util.List;
import java.util.Map; // Upewnij się, że ten import jest

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

// Upewnij się, że nazwa klasy pasuje do nazwy pliku
public class CreateDeckActivity extends AppCompatActivity {

    private TextInputEditText titleEditText, descriptionEditText, cardFrontEditText, cardBackEditText;
    private Button addCardButton, saveDeckButton;
    private RecyclerView addedCardsRecyclerView;

    private List<Flashcard> newCardsList = new ArrayList<>();
    private EditorCardAdapter editorCardAdapter;

    // Klucz API Supabase (zapamiętany)
    private static final String SUPABASE_API_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6IndmaGRuYmdmdXFjaG11bnBtaGV6Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3NjA5NjA0MzEsImV4cCI6MjA3NjUzNjQzMX0.fx6xY49V2hUG0msmo1pvGp9bnkDzKrBMs4iZT7_t8Zo";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_deck);

        // --- POCZĄTEK KODU DLA TOOLBARA ---
        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        ImageView backArrow = findViewById(R.id.toolbar_back_arrow);
        backArrow.setVisibility(View.VISIBLE);
        backArrow.setOnClickListener(v -> onBackPressed());
        // --- KONIEC KODU DLA TOOLBARA ---

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

        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        if (account == null) {
            Toast.makeText(this, "Musisz być zalogowany, aby zapisać talię.", Toast.LENGTH_SHORT).show();
            return;
        }
        String userId = account.getId();

        ApiService apiService = RetrofitClient.getApiService();
        String authorizationHeader = "Bearer " + SUPABASE_API_KEY;

        // Tworzymy Map zamiast obiektu DeckData
        Map<String, Object> deckDataMap = new HashMap<>();
        deckDataMap.put("owner_user_id", userId);
        deckDataMap.put("title", title);
        deckDataMap.put("description", description);

        com.google.gson.Gson gson = new com.google.gson.Gson();
        Log.d("CreateDeckActivity", "Wysyłanie danych talii (Map): " + gson.toJson(deckDataMap));

        Call<List<DeckData>> createDeckCall = apiService.createDeck(deckDataMap, SUPABASE_API_KEY, authorizationHeader);
        createDeckCall.enqueue(new Callback<List<DeckData>>() {
            @Override
            public void onResponse(Call<List<DeckData>> call, Response<List<DeckData>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    long newDeckId = response.body().get(0).getDeckId();
                    Log.i("CreateDeckActivity", "Utworzono talię z ID: " + newDeckId);
                    createFlashcardsForDeck(newDeckId, userId);
                } else {
                    Log.e("CreateDeckActivity", "Błąd tworzenia talii: " + response.code() + " - " + response.message());
                    try {
                        Log.e("CreateDeckActivity", "Error body: " + (response.errorBody() != null ? response.errorBody().string() : "null"));
                    } catch (Exception e) { e.printStackTrace(); }
                    Toast.makeText(CreateDeckActivity.this, "Błąd zapisu talii.", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<List<DeckData>> call, Throwable t) {
                Log.e("CreateDeckActivity", "Błąd sieci przy tworzeniu talii", t);
                Toast.makeText(CreateDeckActivity.this, "Błąd sieci.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void createFlashcardsForDeck(long deckId, String userId) {
        ApiService apiService = RetrofitClient.getApiService();
        String authorizationHeader = "Bearer " + SUPABASE_API_KEY;

        List<FlashcardData> flashcardsToCreate = new ArrayList<>();
        for (Flashcard card : newCardsList) {
            flashcardsToCreate.add(new FlashcardData(deckId, card.getFront(), card.getBack()));
        }

        Call<Void> createCardsCall = apiService.createFlashcards(flashcardsToCreate, SUPABASE_API_KEY, authorizationHeader);
        createCardsCall.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Log.i("CreateDeckActivity", "Dodano fiszki dla talii ID: " + deckId);
                    linkDeckToUser(deckId, userId);
                } else {
                    Log.e("CreateDeckActivity", "Błąd dodawania fiszek: " + response.code() + " - " + response.message());
                    try {
                        Log.e("CreateDeckActivity", "Error body: " + (response.errorBody() != null ? response.errorBody().string() : "null"));
                    } catch (Exception e) { e.printStackTrace(); }
                    Toast.makeText(CreateDeckActivity.this, "Błąd zapisu fiszek.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.e("CreateDeckActivity", "Błąd sieci przy dodawaniu fiszek", t);
                Toast.makeText(CreateDeckActivity.this, "Błąd sieci.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void linkDeckToUser(long deckId, String userId) {
        ApiService apiService = RetrofitClient.getApiService();
        String authorizationHeader = "Bearer " + SUPABASE_API_KEY;
        UserDeckLink link = new UserDeckLink(userId, deckId);

        Call<Void> linkCall = apiService.saveUserDeck(link, SUPABASE_API_KEY, authorizationHeader);
        linkCall.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Log.i("CreateDeckActivity", "Połączono talię ID: " + deckId + " z użytkownikiem ID: " + userId);
                    Toast.makeText(CreateDeckActivity.this, "Zestaw zapisany!", Toast.LENGTH_SHORT).show();
                    finish(); // Sukces - zamknij aktywność
                } else {
                    Log.e("CreateDeckActivity", "Błąd łączenia talii z użytkownikiem: " + response.code() + " - " + response.message());
                    try {
                        Log.e("CreateDeckActivity", "Error body: " + (response.errorBody() != null ? response.errorBody().string() : "null"));
                    } catch (Exception e) { e.printStackTrace(); }
                    Toast.makeText(CreateDeckActivity.this, "Błąd zapisu.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.e("CreateDeckActivity", "Błąd sieci przy łączeniu talii", t);
                Toast.makeText(CreateDeckActivity.this, "Błąd sieci.", Toast.LENGTH_SHORT).show();
            }
        });
    }

} // Upewnij się, że ten nawias zamyka klasę CreateDeckActivity