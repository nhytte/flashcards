package com.example.flashcards; // Upewnij się, że to nazwa Twojego pakietu

import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import java.util.List;

public class LearningActivity extends AppCompatActivity {

    private TextView progressTextView, frontTextView, backTextView;
    private Button previousButton, nextButton;
    private FrameLayout cardContainer;
    private CardView cardFront, cardBack;

    private List<Flashcard> currentCards;
    private int currentCardIndex = 0;
    private boolean isFrontVisible = true;

    private AnimatorSet frontAnim;
    private AnimatorSet backAnim;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_learning);

        // --- POCZĄTEK KODU DLA TOOLBARA ---
        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        // Znajdujemy nasz niestandardowy przycisk i go włączamy
        ImageView backArrow = findViewById(R.id.toolbar_back_arrow);
        backArrow.setVisibility(View.VISIBLE);
        backArrow.setOnClickListener(v -> onBackPressed()); // Ustawiamy akcję cofania

        ImageView iconGoogleLogin = findViewById(R.id.icon_google_login);
        ImageView iconProfile = findViewById(R.id.icon_profile);
        if (iconGoogleLogin != null) {
            iconGoogleLogin.setVisibility(View.GONE);
        }
        if (iconProfile != null) {
            iconProfile.setVisibility(View.GONE);
        }
        // --- KONIEC KODU DLA TOOLBARA ---

        progressTextView = findViewById(R.id.textViewProgress);
        frontTextView = findViewById(R.id.textViewFront);
        backTextView = findViewById(R.id.textViewBack);
        previousButton = findViewById(R.id.buttonPrevious);
        nextButton = findViewById(R.id.buttonNext);
        cardContainer = findViewById(R.id.cardContainer);
        cardFront = findViewById(R.id.cardFront);
        cardBack = findViewById(R.id.cardBack);

        int deckIndex = getIntent().getIntExtra("DECK_INDEX", -1);
        if (deckIndex == -1 || deckIndex >= MainActivity.deckList.size()) {
            Toast.makeText(this, "Błąd: Nie znaleziono zestawu.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        currentCards = MainActivity.deckList.get(deckIndex).getCards();
        if (currentCards == null || currentCards.isEmpty()) {
            Toast.makeText(this, "Ten zestaw nie ma żadnych fiszek!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        loadAnimations();
        setupClickListeners();
        showCurrentCard();
    }

    private void loadAnimations() {
        float scale = getApplicationContext().getResources().getDisplayMetrics().density;
        cardFront.setCameraDistance(8000 * scale);
        cardBack.setCameraDistance(8000 * scale);
        frontAnim = (AnimatorSet) AnimatorInflater.loadAnimator(getApplicationContext(), R.animator.front_animator);
        backAnim = (AnimatorSet) AnimatorInflater.loadAnimator(getApplicationContext(), R.animator.back_animator);
    }

    private void setupClickListeners() {
        cardContainer.setOnClickListener(v -> flipCard());
        nextButton.setOnClickListener(v -> {
            if (currentCardIndex < currentCards.size() - 1) {
                currentCardIndex++;
                showCurrentCard();
            }
        });
        previousButton.setOnClickListener(v -> {
            if (currentCardIndex > 0) {
                currentCardIndex--;
                showCurrentCard();
            }
        });
    }

    // W pliku LearningActivity.java

    private void showCurrentCard() {
        // --- POCZĄTEK RESETOWANIA STANU ---
        // Upewnij się, że zawsze zaczynamy od przodu
        isFrontVisible = true;
        // Resetujemy obroty i alfa na wszelki wypadek (choć visibility powinno wystarczyć)
        cardFront.setRotationY(0);
        cardFront.setAlpha(1.0f);
        cardBack.setRotationY(0); // Można też użyć wartości startowej z animatora, np. -180
        cardBack.setAlpha(1.0f); // Ustawiamy alfa na 1, visibility ukryje
        // Ustawiamy poprawną widoczność
        cardFront.setVisibility(View.VISIBLE); // Pokaż przód
        cardBack.setVisibility(View.GONE);    // Ukryj tył
        // --- KONIEC RESETOWANIA STANU ---

        // Wczytaj dane nowej fiszki
        Flashcard card = currentCards.get(currentCardIndex);
        frontTextView.setText(card.getFront());
        backTextView.setText(card.getBack());
        progressTextView.setText((currentCardIndex + 1) + "/" + currentCards.size());

        // Aktualizuj widoczność przycisków nawigacyjnych
        previousButton.setVisibility(currentCardIndex == 0 ? View.INVISIBLE : View.VISIBLE);
        nextButton.setVisibility(currentCardIndex == currentCards.size() - 1 ? View.INVISIBLE : View.VISIBLE);
    }

    private void flipCard() {
        if (isFrontVisible) {
            frontAnim.setTarget(cardFront);
            backAnim.setTarget(cardBack);
            cardBack.setVisibility(View.VISIBLE);
            frontAnim.start();
            backAnim.start();
            isFrontVisible = false;
        } else {
            frontAnim.setTarget(cardBack);
            backAnim.setTarget(cardFront);
            backAnim.start();
            frontAnim.start();
            isFrontVisible = true;
        }
    }

    // NIE POTRZEBUJEMY JUŻ onSupportNavigateUp()
}