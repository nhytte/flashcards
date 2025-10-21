package com.example.flashcards; // Upewnij się, że to nazwa Twojego pakietu

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // NIE UŻYWAMY setContentView()!
        // Motyw w manifeście już ustawił nasze tło.

        // Po prostu przejdź do MainActivity po krótkim opóźnieniu
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(SplashActivity.this, MainActivity.class);
                startActivity(intent);
                finish(); // Ważne: zamknij splash screen
            }
        }, 1000); // 1 sekunda (możesz zmienić)
    }
}