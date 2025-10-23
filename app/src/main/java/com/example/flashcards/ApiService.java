package com.example.flashcards;

import java.util.ArrayList; // Upewnij się, że ten import jest
import java.util.List;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Query;
import java.util.Map;

// --- POCZĄTEK INTERFEJSU ---
public interface ApiService {

    // --- Users ---
    @Headers({
            "Content-Type: application/json",
            "Prefer: resolution=merge-duplicates"
    })
    @POST("rest/v1/Users")
    Call<Void> upsertUser(
            @Body UserData user,
            @Header("apikey") String apiKey,
            @Header("Authorization") String authorization
    );

    // --- Decks ---
    @GET("rest/v1/userdecks")
    Call<List<UserDeckRelation>> getUserDecks(
            @Query("user_id") String userIdEq,
            @Query("select") String select,
            @Header("apikey") String apiKey,
            @Header("Authorization") String authorization
    );

    @Headers({
            "Content-Type: application/json",
            "Prefer: return=representation"
    })
    @POST("rest/v1/decks")
    Call<List<DeckData>> createDeck(
            @Body Map<String, Object> deckDataMap, // <-- Zmieniono na Map
            @Header("apikey") String apiKey,
            @Header("Authorization") String authorization
    );

    // --- Flashcards ---
    @Headers({"Content-Type: application/json"})
    @POST("rest/v1/flashcards")
    Call<Void> createFlashcards(
            @Body List<FlashcardData> flashcards,
            @Header("apikey") String apiKey,
            @Header("Authorization") String authorization
    );

    // --- UserDecks (powiązania) ---
    @Headers({
            "Content-Type: application/json",
            "Prefer: resolution=ignore-duplicates"
    })
    @POST("rest/v1/userdecks")
    Call<Void> saveUserDeck(
            @Body UserDeckLink userDeckLink,
            @Header("apikey") String apiKey,
            @Header("Authorization") String authorization
    );

    @DELETE("rest/v1/userdecks")
    Call<Void> deleteUserDeck(
            @Query("user_id") String userIdEq,
            @Query("deck_id") String deckIdEq,
            @Header("apikey") String apiKey,
            @Header("Authorization") String authorization
    );

} // --- KONIEC INTERFEJSU ApiService ---


// --- POCZĄTEK KLAS DANYCH (POZA INTERFEJSEM!) ---

// W pliku ApiService.java

class DeckData {
    public int deck_id;
    String owner_user_id;
    String title;
    String description;

    // --- UPEWNIJ SIĘ, ŻE NIE MA TU 'transient' ---
    @com.google.gson.annotations.SerializedName("flashcards")
    List<FlashcardData> flashcards;
    // --- KONIEC SPRAWDZANIA ---

    // Konstruktory i gettery bez zmian...
    public DeckData(String owner_user_id, String title, String description) {
        this.owner_user_id = owner_user_id;
        this.title = title;
        this.description = description;
        this.flashcards = new ArrayList<>();
    }
    public DeckData() { this.flashcards = new ArrayList<>(); }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public int getDeckId() { return deck_id; }
    public List<FlashcardData> getFlashcards() { return flashcards != null ? flashcards : new ArrayList<>(); }
}

class FlashcardData {
    long deck_id;
    String front_text;
    String back_text;

    public FlashcardData(long deck_id, String front_text, String back_text) {
        this.deck_id = deck_id;
        this.front_text = front_text;
        this.back_text = back_text;
    }
}

class UserDeckLink {
    String user_id;
    long deck_id;

    public UserDeckLink(String user_id, long deck_id) {
        this.user_id = user_id;
        this.deck_id = deck_id;
    }
}

class UserDeckRelation {
    public int deck_id;
    public DeckData decks; // Zmienione na małe 'd'

    public Deck toDeckModel() {
        if (decks != null) {
            // Konwertuj listę fiszek
            List<Flashcard> actualFlashcards = new ArrayList<>();
            List<FlashcardData> downloadedFlashcards = decks.getFlashcards();
            if (downloadedFlashcards != null) {
                for (FlashcardData fcData : downloadedFlashcards) {
                    if (fcData.front_text != null && fcData.back_text != null) {
                        actualFlashcards.add(new Flashcard(fcData.front_text, fcData.back_text));
                    }
                }
            }
            return new Deck(decks.getTitle(), decks.getDescription(), actualFlashcards);
        }
        return null;
    }
    public int getActualDeckId() {
        return (decks != null) ? decks.getDeckId() : 0; // Pobierz ID z obiektu DeckData
    }
    public int getDeckId() { return deck_id; }
}

// --- KONIEC KLAS DANYCH ---