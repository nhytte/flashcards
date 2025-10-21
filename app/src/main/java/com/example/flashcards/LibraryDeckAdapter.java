package com.example.flashcards; // Upewnij się, że to nazwa Twojego pakietu

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class LibraryDeckAdapter extends RecyclerView.Adapter<LibraryDeckAdapter.ViewHolder> {

    private List<Deck> libraryDecks;
    private OnDeckAddListener listener;

    // Interfejs do komunikacji z Aktywnością
    public interface OnDeckAddListener {
        void onDeckAdded(Deck deck);
    }

    public LibraryDeckAdapter(List<Deck> libraryDecks, OnDeckAddListener listener) {
        this.libraryDecks = libraryDecks;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_library_deck, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Deck deck = libraryDecks.get(position);
        holder.title.setText(deck.getTitle());
        holder.description.setText(deck.getDescription());
        holder.cardCount.setText(deck.getCardCount() + " kart");
    }

    @Override
    public int getItemCount() {
        return libraryDecks.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView title, description, cardCount;
        Button addButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.textViewLibraryTitle);
            description = itemView.findViewById(R.id.textViewLibraryDescription);
            cardCount = itemView.findViewById(R.id.textViewLibraryCardCount);
            addButton = itemView.findViewById(R.id.buttonAddFromLibrary);

            addButton.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    listener.onDeckAdded(libraryDecks.get(position));
                }
            });
        }
    }
}