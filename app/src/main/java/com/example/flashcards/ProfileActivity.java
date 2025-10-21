package com.example.flashcards;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

public class ProfileActivity extends AppCompatActivity {

    private TextView textViewUserName;
    private Button buttonLogout;
    private GoogleSignInClient mGoogleSignInClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        setupToolbar();

        textViewUserName = findViewById(R.id.textViewUserName);
        buttonLogout = findViewById(R.id.buttonLogout);

        // Odbierz nazwę użytkownika z Intentu
        String userName = getIntent().getStringExtra("USER_NAME");
        if (userName != null && !userName.isEmpty()) {
            textViewUserName.setText(userName);
        } else {
            textViewUserName.setText("Użytkownik (brak nazwy)");
        }

        // Konfiguracja Google Sign-In do wylogowania
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("428819652685-2n0rrvsr3bodtagmuljo7k7fi246rv3c.apps.googleusercontent.com") // !!! WAŻNE !!! Użyj tego samego ID co w MainActivity
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);


        buttonLogout.setOnClickListener(v -> signOut());
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
        ImageView backArrow = findViewById(R.id.toolbar_back_arrow);
        backArrow.setVisibility(View.VISIBLE);
        backArrow.setOnClickListener(v -> onBackPressed());

        TextView toolbarTitle = findViewById(R.id.toolbar_title);
        toolbarTitle.setText("Profil"); // Ustaw tytuł paska na "Profil"

        // Ukryj ikony logowania/profilu na stronie profilu
        ImageView iconGoogleLogin = findViewById(R.id.icon_google_login);
        ImageView iconProfile = findViewById(R.id.icon_profile);
        iconGoogleLogin.setVisibility(View.GONE);
        iconProfile.setVisibility(View.GONE);
    }

    private void signOut() {
        mGoogleSignInClient.signOut()
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(Task<Void> task) {
                        Toast.makeText(ProfileActivity.this, "Wylogowano pomyślnie", Toast.LENGTH_SHORT).show();
                        // Po wylogowaniu wróć do MainActivity
                        Intent intent = new Intent(ProfileActivity.this, MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        finish(); // Zakończ ProfileActivity
                    }
                });
    }
}