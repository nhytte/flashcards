package com.example.flashcards; // <-- ZMIEŃ NA NAZWĘ SWOJEGO PAKIETU

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class DeckAdapter extends RecyclerView.Adapter<DeckAdapter.DeckViewHolder> {

    private List<Deck> deckList;
    private OnDeckClickListener listener;

    public interface OnDeckClickListener {
        void onPracticeClick(int position);
        void onDeleteClick(int position);
    }

    public DeckAdapter(List<Deck> deckList, Context context, OnDeckClickListener listener) {
        this.deckList = deckList;
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

            buttonPractice.setOnClickListener(v -> {
                if (listener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        listener.onPracticeClick(position);
                    }
                }
            });

            buttonDelete.setOnClickListener(v -> {
                if (listener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        listener.onDeleteClick(position);
                    }
                }
            });
        }
    }
}