package com.example.flashcards;

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

public class MainActivity extends AppCompatActivity {

    public static List<Deck> deckList = new ArrayList<>(); // Lista fiszek dla użytkownika

    private RecyclerView recyclerView;
    private DeckAdapter adapter;
    private FloatingActionButton fab;

    // Google Sign-In
    private GoogleSignInClient mGoogleSignInClient;
    private ActivityResultLauncher<Intent> signInLauncher;
    private ImageView iconGoogleLogin;
    private ImageView iconProfile;
    private TextView toolbarTitle; // Dodajemy TextView dla tytułu paska
    private String userName = "Gość"; // Domyślna nazwa użytkownika

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setupToolbar();
        setupGoogleSignIn();
        setupFAB();
        setupRecyclerView();

        // Jeśli lista jest pusta, dodaj jeden przykładowy zestaw
        if (deckList.isEmpty()) {
            List<Flashcard> sampleCards = new ArrayList<>();
            sampleCards.add(new Flashcard("Hola", "Cześć"));
            sampleCards.add(new Flashcard("Adiós", "Do widzenia"));
            sampleCards.add(new Flashcard("Por favor", "Proszę"));
            deckList.add(new Deck("Słownictwo hiszpańskie", "Podstawowe hiszpańskie słowa i zwroty", sampleCards));
            adapter.notifyDataSetChanged();
        }
    }

    private void setupToolbar() {
        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        iconGoogleLogin = findViewById(R.id.icon_google_login);
        iconProfile = findViewById(R.id.icon_profile);
        toolbarTitle = findViewById(R.id.toolbar_title); // Inicjalizacja TextView

        // Ustaw kliknięcie dla ikony Google
        iconGoogleLogin.setOnClickListener(v -> signIn());

        // Ustaw kliknięcie dla ikony Profilu
        iconProfile.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
            intent.putExtra("USER_NAME", userName); // Przekaż nazwę użytkownika do Profilu
            startActivity(intent);
        });

        updateToolbarIcons(); // Aktualizuj ikony przy starcie
    }

    private void setupGoogleSignIn() {
        // Konfiguracja Google Sign-In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("428819652685-2n0rrvsr3bodtagmuljo7k7fi246rv3c.apps.googleusercontent.com") // !!! WAŻNE !!! ZASTĄP SWOIM WEB_CLIENT_ID
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        // Ustawienie launchera dla wyniku logowania
        signInLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        Intent data = result.getData();
                        Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
                        handleSignInResult(task);
                    } else {
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
            // Zalogowanie powiodło się, pobierz informacje o użytkowniku
            if (account != null) {
                userName = account.getDisplayName() != null ? account.getDisplayName() : account.getEmail();
                if (userName == null || userName.isEmpty()) {
                    userName = "Zalogowany użytkownik"; // Domyślna nazwa, jeśli brak displayName
                }
                Toast.makeText(this, "Witaj, " + userName + "!", Toast.LENGTH_SHORT).show();
                updateToolbarIcons();
                // Możesz zapisać ID token lub inne dane użytkownika, aby użyć ich na serwerze
                String idToken = account.getIdToken();
                Log.d("MainActivity", "ID Token: " + idToken);
            }
        } catch (ApiException e) {
            Log.w("MainActivity", "signInResult:failed code=" + e.getStatusCode());
            Toast.makeText(this, "Błąd logowania: " + e.getStatusCode(), Toast.LENGTH_LONG).show();
            userName = "Gość"; // Przywróć stan gościa w przypadku błędu
            updateToolbarIcons();
        }
    }

    private void updateToolbarIcons() {
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        if (account != null) {
            // Użytkownik jest zalogowany
            iconGoogleLogin.setVisibility(View.GONE);
            iconProfile.setVisibility(View.VISIBLE);
            userName = account.getDisplayName() != null ? account.getDisplayName() : "Zalogowany użytkownik";
            toolbarTitle.setText("FlashCards"); // Tytuł aplikacji
        } else {
            // Użytkownik nie jest zalogowany
            iconGoogleLogin.setVisibility(View.VISIBLE);
            iconProfile.setVisibility(View.GONE);
            userName = "Gość";
            toolbarTitle.setText("FlashCards"); // Tytuł aplikacji
        }
    }


    private void setupFAB() {
        fab = findViewById(R.id.fab_add_deck);
        fab.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, AddDeckActivity.class);
            startActivity(intent);
        });
    }

    private void setupRecyclerView() {
        recyclerView = findViewById(R.id.recyclerViewDecks); // Upewnij się, że recyclerView jest zainicjowane
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

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
                    adapter.notifyItemRemoved(position); // Użyj adaptera, aby powiadomić o usunięciu
                    Toast.makeText(MainActivity.this, "Usunięto zestaw", Toast.LENGTH_SHORT).show();
                }
            }
        };

        adapter = new DeckAdapter(deckList, this, listener); // Tworzenie adaptera z 3 argumentami
        recyclerView.setAdapter(adapter); // Ustawienie adaptera dla RecyclerView
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Sprawdź stan logowania za każdym razem, gdy aktywność wraca na pierwszy plan
        updateToolbarIcons();
        // Odśwież listę fiszek
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }
}