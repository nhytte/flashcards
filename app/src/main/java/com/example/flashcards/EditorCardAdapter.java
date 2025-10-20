package com.example.flashcards; // <-- ZMIEŃ NA NAZWĘ SWOJEGO PAKIETU

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class EditorCardAdapter extends RecyclerView.Adapter<EditorCardAdapter.ViewHolder> {

    private List<Flashcard> cardList;

    public EditorCardAdapter(List<Flashcard> cardList) {
        this.cardList = cardList;
    }

    @NonNull @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_flashcard_editor, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Flashcard card = cardList.get(position);
        holder.frontText.setText(card.getFront());
        holder.backText.setText(card.getBack());
    }

    @Override
    public int getItemCount() {
        return cardList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView frontText, backText;
        ImageButton deleteButton;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            frontText = itemView.findViewById(R.id.textViewEditorFront);
            backText = itemView.findViewById(R.id.textViewEditorBack);
            deleteButton = itemView.findViewById(R.id.buttonEditorDelete);

            deleteButton.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    cardList.remove(position);
                    notifyItemRemoved(position);
                }
            });
        }
    }
}