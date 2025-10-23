package com.example.flashcards; // Upewnij się, że to nazwa Twojego pakietu

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DeckAdapter extends RecyclerView.Adapter<DeckAdapter.DeckViewHolder> {

    private List<Deck> deckList;
    private Context context; // Przechowujemy kontekst
    private OnDeckClickListener listener;

    // !!! ZASTĄP SWOIM KLUCZEM !!!
    private static final String SUPABASE_API_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6IndmaGRuYmdmdXFjaG11bnBtaGV6Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3NjA5NjA0MzEsImV4cCI6MjA3NjUzNjQzMX0.fx6xY49V2hUG0msmo1pvGp9bnkDzKrBMs4iZT7_t8Zo";

    // Interfejs do obsługi kliknięć
    public interface OnDeckClickListener {
        void onPracticeClick(int position);
        void onDeleteClick(int position); // Ten interfejs może nie być już potrzebny, bo usuwanie obsługujemy tu
    }

    public DeckAdapter(List<Deck> deckList, Context context, OnDeckClickListener listener) {
        this.deckList = deckList;
        this.context = context; // Przypisujemy kontekst
        this.listener = listener;
    }

    @NonNull
    @Override
    public DeckViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_deck, parent, false);
        return new DeckViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull DeckViewHolder holder, int position) {
        Deck currentDeck = deckList.get(position);
        holder.title.setText(currentDeck.getTitle());
        holder.description.setText(currentDeck.getDescription());
        holder.cardCount.setText(currentDeck.getCardCount() + " kart");
    }

    @Override
    public int getItemCount() {
        return deckList.size();
    }

    // ViewHolder - przechowuje widoki dla pojedynczego elementu
    // Inner ViewHolder class within DeckAdapter.java
    class DeckViewHolder extends RecyclerView.ViewHolder {
        TextView title, description, cardCount;
        Button buttonPractice, buttonDelete;

        public DeckViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.textViewDeckTitle);
            description = itemView.findViewById(R.id.textViewDeckDescription);
            cardCount = itemView.findViewById(R.id.textViewCardCount);
            buttonPractice = itemView.findViewById(R.id.buttonPractice);
            buttonDelete = itemView.findViewById(R.id.buttonDelete);

            // Click listener for the "Practice" button
            buttonPractice.setOnClickListener(v -> {
                if (listener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        listener.onPracticeClick(position);
                    }
                }
            });

            // Click listener for the "Delete" button
            buttonDelete.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    // Get the logged-in user
                    GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(itemView.getContext());
                    if (account == null) {
                        Toast.makeText(itemView.getContext(), "Błąd: Nie jesteś zalogowany.", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    String userId = account.getId();
                    // Get the deck ID from the model object
                    int deckId = deckList.get(position).getDbId();

                    // --- ADDED LOGGING HERE ---
                    Log.d("DeckAdapter", "Próba usunięcia talii: " + deckList.get(position).getTitle() + " z ID: " + deckId);
                    // --- END OF ADDED LOGGING ---

                    // Check if the ID is valid before calling the API
                    if (deckId <= 0) {
                        Toast.makeText(itemView.getContext(), "Błąd: Brak ID talii.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // Call the API method to delete the user-deck link
                    deleteUserDeckLink(itemView.getContext(), userId, deckId, position);
                }
            });
        }
    } // End of DeckViewHolder class

    // Metoda do usuwania powiązania Użytkownik <-> Talia z bazy danych
    private void deleteUserDeckLink(Context context, String userId, int deckId, int position) {
        ApiService apiService = RetrofitClient.getApiService();
        String authorizationHeader = "Bearer " + SUPABASE_API_KEY;
        String userIdParam = "eq." + userId;
        String deckIdParam = "eq." + deckId;

        Call<Void> call = apiService.deleteUserDeck(userIdParam, deckIdParam, SUPABASE_API_KEY, authorizationHeader);
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                // Sprawdzamy czy sukces LUB czy zasób nie został znaleziony (już usunięty)
                if (response.isSuccessful() || response.code() == 404 || response.code() == 204 ) {
                    Log.i("DeckAdapter", "Usunięto powiązanie dla talii ID: " + deckId);
                    // Usuń z listy lokalnej i powiadom adapter TYLKO po sukcesie API
                    // Upewnij się, że pozycja jest nadal prawidłowa
                    if (position < deckList.size() && deckList.get(position).getDbId() == deckId) {
                        deckList.remove(position);
                        notifyItemRemoved(position);
                        // Opcjonalnie: powiadom o zmianie zakresu, jeśli usunięcie mogło wpłynąć na inne pozycje
                        // notifyItemRangeChanged(position, deckList.size());
                        Toast.makeText(context, "Usunięto zestaw", Toast.LENGTH_SHORT).show();
                    } else {
                        Log.w("DeckAdapter", "Pozycja do usunięcia była nieprawidłowa po odpowiedzi API.");
                        // Możesz spróbować odświeżyć całą listę z serwera w takim przypadku
                    }

                } else {
                    Log.e("DeckAdapter", "Błąd usuwania powiązania: " + response.code() + " " + response.message());
                    try {
                        Log.e("DeckAdapter", "Error body: " + (response.errorBody() != null ? response.errorBody().string() : "null"));
                    } catch (Exception e) { e.printStackTrace(); }
                    Toast.makeText(context, "Błąd usuwania.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.e("DeckAdapter", "Błąd sieci przy usuwaniu powiązania", t);
                Toast.makeText(context, "Błąd sieci.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}