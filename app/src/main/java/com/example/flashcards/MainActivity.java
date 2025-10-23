package com.example.flashcards; // Upewnij się, że to nazwa Twojego pakietu

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
// import androidx.core.splashscreen.SplashScreen; // <-- USUNIĘTY IMPORT
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class MainActivity extends AppCompatActivity {

    public static List<Deck> deckList = new ArrayList<>();

    private RecyclerView recyclerView;
    private DeckAdapter adapter;
    private FloatingActionButton fab;

    // Google Sign-In
    private GoogleSignInClient mGoogleSignInClient;
    private ActivityResultLauncher<Intent> signInLauncher;
    private ImageView iconGoogleLogin;
    private ImageView iconProfile;
    private TextView toolbarTitle;
    private String userName = "Gość";

    // !!! ZASTĄP SWOIM KLUCZEM I URL !!!
    private static final String SUPABASE_API_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6IndmaGRuYmdmdXFjaG11bnBtaGV6Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3NjA5NjA0MzEsImV4cCI6MjA3NjUzNjQzMX0.fx6xY49V2hUG0msmo1pvGp9bnkDzKrBMs4iZT7_t8Zo";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // --- USUNIĘTA LINIA SPLASH SCREEN ---
        // SplashScreen splashScreen = SplashScreen.installSplashScreen(this);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setupToolbar();
        setupGoogleSignIn();
        setupFAB();
        setupRecyclerView(); // Inicjalizacja adaptera i RecyclerView

        // Sprawdź stan logowania przy starcie
        updateToolbarIcons(); // To wywoła loadUserDecks jeśli zalogowany
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false); // Wyłącz domyślny tytuł
        }

        iconGoogleLogin = findViewById(R.id.icon_google_login);
        iconProfile = findViewById(R.id.icon_profile);
        toolbarTitle = findViewById(R.id.toolbar_title); // Inicjalizacja TextView tytułu z layoutu

        iconGoogleLogin.setOnClickListener(v -> signIn());

        iconProfile.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
            intent.putExtra("USER_NAME", userName);
            startActivity(intent);
        });
    }

    private void setupGoogleSignIn() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("428819652685-2n0rrvsr3bodtagmuljo7k7fi246rv3c.apps.googleusercontent.com") // !!! ZASTĄP SWOIM WEB_CLIENT_ID !!!
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        signInLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        Intent data = result.getData();
                        Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
                        handleSignInResult(task);
                    } else {
                        Log.w("MainActivity", "Google Sign in cancelled or failed. Result Code: " + result.getResultCode());
                        Toast.makeText(MainActivity.this, "Logowanie anulowane", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        signInLauncher.launch(signInIntent);
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            if (account != null) {
                String userId = account.getId();
                String displayName = account.getDisplayName();
                String email = account.getEmail();

                if (displayName == null || displayName.isEmpty()) {
                    displayName = email != null ? email.split("@")[0] : "Zalogowany użytkownik";
                }
                userName = displayName;

                sendUserToSupabase(new UserData(userId, displayName, email)); // Zapisz/Zaktualizuj usera w Supabase

                // Od razu zaktualizuj UI i załaduj talie
                updateToolbarIcons(); // To wywoła też loadUserDecks

                Toast.makeText(this, "Witaj, " + userName + "!", Toast.LENGTH_SHORT).show();

            } else {
                Log.w("MainActivity", "GoogleSignInAccount is null after successful sign in.");
                signOut(); // Wyloguj w razie dziwnego błędu
            }
        } catch (ApiException e) {
            Log.e("MainActivity", "signInResult:failed code=" + e.getStatusCode(), e);
            Toast.makeText(this, "Błąd logowania Google: " + e.getMessage(), Toast.LENGTH_LONG).show();
            signOut(); // Wyloguj w razie błędu
        }
    }

    private void sendUserToSupabase(UserData userData) {
        ApiService apiService = RetrofitClient.getApiService();
        String authorizationHeader = "Bearer " + SUPABASE_API_KEY;

        Call<Void> call = apiService.upsertUser(userData, SUPABASE_API_KEY, authorizationHeader);
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Log.i("MainActivity", "User data sent/updated successfully in Supabase.");
                } else {
                    Log.e("MainActivity", "Supabase API error (upsertUser): " + response.code() + " - " + response.message());
                    try {
                        Log.e("MainActivity", "Error body: " + (response.errorBody() != null ? response.errorBody().string() : "null"));
                    } catch (Exception e) { e.printStackTrace(); }
                    Toast.makeText(MainActivity.this, "Błąd synchronizacji profilu z serwerem.", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.e("MainActivity", "Network error sending user data to Supabase", t);
                Toast.makeText(MainActivity.this, "Błąd sieci. Sprawdź połączenie.", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void updateToolbarIcons() {
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        if (account != null) {
            iconGoogleLogin.setVisibility(View.GONE);
            iconProfile.setVisibility(View.VISIBLE);
            userName = account.getDisplayName() != null && !account.getDisplayName().isEmpty()
                    ? account.getDisplayName()
                    : (account.getEmail() != null ? account.getEmail().split("@")[0] : "Zalogowany użytkownik");
            if (toolbarTitle != null) toolbarTitle.setText("FlashCards");

            loadUserDecks(account.getId());

        } else {
            iconGoogleLogin.setVisibility(View.VISIBLE);
            iconProfile.setVisibility(View.GONE);
            userName = "Gość";
            if (toolbarTitle != null) toolbarTitle.setText("FlashCards");

            if (deckList != null && !deckList.isEmpty()){
                deckList.clear();
                if(adapter != null) adapter.notifyDataSetChanged();
            }
        }
    }


    private void loadUserDecks(String userId) {
        Log.d("MainActivity", "Rozpoczynam ładowanie talii dla użytkownika: " + userId); // Log startu
        ApiService apiService = RetrofitClient.getApiService();
        // Pamiętaj o użyciu swojego klucza API
        String authorizationHeader = "Bearer " + SUPABASE_API_KEY;
        String userIdParam = "eq." + userId; // Format Supabase: "eq.USER_ID"
        // Pobieramy talie ORAZ zagnieżdżone fiszki
        String selectParam = "deck_id,decks(*,flashcards(*))";

        Call<List<UserDeckRelation>> call = apiService.getUserDecks(userIdParam, selectParam, SUPABASE_API_KEY, authorizationHeader);

        call.enqueue(new Callback<List<UserDeckRelation>>() {
            @Override
            public void onResponse(Call<List<UserDeckRelation>> call, Response<List<UserDeckRelation>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Deck> newDeckList = new ArrayList<>(); // Stwórz nową listę tymczasową
                    // Przetwarzanie odpowiedzi
                    for(UserDeckRelation relation : response.body()) {
                        // Sprawdź, czy talia została poprawnie dołączona i ma ID > 0
                        if (relation.decks == null || relation.getActualDeckId() <= 0) {
                            Log.w("MainActivity", "Pominięto nieprawidłową relację dla deck_id (z userdecks): " + relation.getDeckId());
                            continue; // Przejdź do następnej relacji
                        }

                        // Konwertuj pobrane fiszki (FlashcardData) na model aplikacji (Flashcard)
                        List<Flashcard> actualFlashcards = new ArrayList<>();
                        List<FlashcardData> downloadedFlashcards = relation.decks.getFlashcards(); // Pobierz listę fiszek z DeckData
                        // Dodaj logowanie liczby pobranych fiszek
                        Log.d("MainActivity", "Pobrano fiszek dla talii '" + relation.decks.getTitle() + "': " + (downloadedFlashcards != null ? downloadedFlashcards.size() : 0));
                        if (downloadedFlashcards != null) {
                            for (FlashcardData fcData : downloadedFlashcards) {
                                if (fcData.front_text != null && fcData.back_text != null) {
                                    actualFlashcards.add(new Flashcard(fcData.front_text, fcData.back_text));
                                }
                            }
                        }

                        // Stwórz obiekt Deck z przekonwertowaną listą fiszek
                        Deck deck = new Deck(relation.decks.getTitle(), relation.decks.getDescription(), actualFlashcards);
                        deck.setDbId(relation.getActualDeckId()); // Ustaw ID z bazy danych
                        newDeckList.add(deck);
                        Log.d("MainActivity", "Przypisano ID: " + relation.getActualDeckId() + " do talii: " + deck.getTitle());
                    }

                    // Bezpieczna aktualizacja listy i powiadomienie adaptera w wątku UI
                    runOnUiThread(() -> {
                        deckList.clear(); // Wyczyść starą listę
                        deckList.addAll(newDeckList); // Dodaj nowe elementy
                        if (adapter != null) {
                            adapter.notifyDataSetChanged(); // Odśwież widok
                        }
                        Log.d("MainActivity", "Załadowano talie: " + deckList.size() + ", powiadamiam adapter.");
                    });

                } else {
                    // Obsługa błędu odpowiedzi API
                    Log.e("MainActivity", "Błąd pobierania talii: " + response.code() + " " + response.message());
                    try {
                        Log.e("MainActivity", "Error body: " + (response.errorBody() != null ? response.errorBody().string() : "null"));
                    } catch (Exception e) { e.printStackTrace(); }
                    if (!isFinishing()) {
                        runOnUiThread(() -> Toast.makeText(MainActivity.this, "Nie udało się pobrać talii.", Toast.LENGTH_SHORT).show());
                    }
                }
            }

            @Override
            public void onFailure(Call<List<UserDeckRelation>> call, Throwable t) {
                // Obsługa błędu sieciowego
                Log.e("MainActivity", "Błąd sieci przy pobieraniu talii", t);
                if (!isFinishing()) {
                    runOnUiThread(() -> Toast.makeText(MainActivity.this, "Błąd sieci. Sprawdź połączenie.", Toast.LENGTH_SHORT).show());
                }
            }
        });
    }


    private void signOut() {
        mGoogleSignInClient.signOut().addOnCompleteListener(this, task -> {
            userName = "Gość";
            updateToolbarIcons(); // To wyczyści listę talii
            Toast.makeText(MainActivity.this, "Wylogowano", Toast.LENGTH_SHORT).show();
        });
    }

    private void setupFAB() {
        fab = findViewById(R.id.fab_add_deck);
        fab.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, AddDeckActivity.class);
            startActivity(intent);
        });
    }

    private void setupRecyclerView() {
        recyclerView = findViewById(R.id.recyclerViewDecks);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        DeckAdapter.OnDeckClickListener listener = new DeckAdapter.OnDeckClickListener() {
            @Override
            public void onPracticeClick(int position) {
                if (position >= 0 && position < deckList.size()) {
                    Intent intent = new Intent(MainActivity.this, LearningActivity.class);
                    intent.putExtra("DECK_INDEX", position);
                    startActivity(intent);
                } else {
                    Log.e("MainActivity", "Nieprawidłowa pozycja kliknięcia: " + position);
                }
            }
            @Override
            public void onDeleteClick(int position) {
                // Ta metoda w interfejsie nie jest już potrzebna
            }
        };

        adapter = new DeckAdapter(deckList, this, listener);
        recyclerView.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d("MainActivity", "onResume called"); // Log wywołania onResume
        updateToolbarIcons(); // To powinno odświeżyć stan zalogowania i wywołać ładowanie talii
    }
}