package io.github.android.gui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import io.github.fortheoil.R;

public class DeckAdapter extends RecyclerView.Adapter<DeckAdapter.DeckViewHolder> {

    private List<Integer> cards;
    private OnCardClickListener listener;

    // Interface pour gérer les clics des boutons
    public interface OnCardClickListener {
        void onAddClick(int position);
        void onInfoClick(int position);
    }

    public DeckAdapter(List<Integer> cards, OnCardClickListener listener) {
        this.cards = cards;
        this.listener = listener;
    }

    // ViewHolder
    public static class DeckViewHolder extends RecyclerView.ViewHolder {
        ImageView image;
        Button btnAdd;
        Button btnInfo;

        public DeckViewHolder(View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.cardImage);
            btnAdd = itemView.findViewById(R.id.btnAddCard);
            btnInfo = itemView.findViewById(R.id.btnInfoCard);
        }
    }

    @Override
    public DeckViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.card_deck_menu, parent, false);
        return new DeckViewHolder(view);
    }

    @Override
    public void onBindViewHolder(DeckViewHolder holder, int position) {
        holder.image.setImageResource(cards.get(position));

        // Actions des boutons
        holder.btnAdd.setOnClickListener(v -> {
            if (listener != null) listener.onAddClick(position);
        });

        holder.btnInfo.setOnClickListener(v -> {
            if (listener != null) listener.onInfoClick(position);
        });
    }

    @Override
    public int getItemCount() {
        return cards.size();
    }
}
