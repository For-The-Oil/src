package io.github.android.gui.adapter;

import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import io.github.android.gui.Card;
import io.github.fortheoil.R;

public class DeckAdapter extends RecyclerView.Adapter<DeckAdapter.DeckViewHolder> {

    private List<Card> cards;
    private int openedPosition = RecyclerView.NO_POSITION;
    private final OnCardActionListener listener;

    public interface OnCardActionListener {
        void onAddClick(Card card, int position);
        void onInfoClick(Card card, int position);
    }

    public DeckAdapter(List<Card> cards, OnCardActionListener listener) {
        this.cards = cards;
        this.listener = listener;
    }

    public static class DeckViewHolder extends RecyclerView.ViewHolder {
        ImageView cardImage;
        LinearLayout cardActions;
        Button btnAdd;
        Button btnInfo;

        public DeckViewHolder(View itemView) {
            super(itemView);
            cardImage = itemView.findViewById(R.id.cardImage);
            cardActions = itemView.findViewById(R.id.cardActions);
            btnAdd = itemView.findViewById(R.id.btnAdd);
            btnInfo = itemView.findViewById(R.id.btnInfo);
        }
    }

    @Override
    public DeckViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.item_deck_card, parent, false);
        return new DeckViewHolder(view);
    }

    @Override
    public void onBindViewHolder(DeckViewHolder holder, int position) {
        Card card = cards.get(position);
        holder.cardImage.setImageResource(card.getImageResId());

        // largeur 25%
        ViewGroup.LayoutParams lp = holder.itemView.getLayoutParams();
        lp.width = (int) (holder.itemView.getResources().getDisplayMetrics().widthPixels * 0.25f);
        holder.itemView.setLayoutParams(lp);

        boolean isOpened = position == openedPosition;

        holder.cardImage.animate().cancel();
        holder.cardActions.animate().cancel();

        if (isOpened) {
            holder.cardImage.animate().scaleX(1.05f).scaleY(1.05f).setDuration(180).start();
            holder.cardActions.setVisibility(View.VISIBLE);
            holder.cardActions.setAlpha(1f);
            holder.cardActions.setTranslationY(0f);
        } else {
            holder.cardImage.setScaleX(1f);
            holder.cardImage.setScaleY(1f);
            holder.cardActions.setAlpha(0f);
            holder.cardActions.setTranslationY(dp(holder.itemView, 16));
            holder.cardActions.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(v -> {
            int oldPos = openedPosition;
            int newPos = holder.getBindingAdapterPosition();
            if (newPos == RecyclerView.NO_POSITION) return;

            openedPosition = (openedPosition == newPos)
                ? RecyclerView.NO_POSITION
                : newPos;

            if (oldPos != RecyclerView.NO_POSITION) notifyItemChanged(oldPos);
            notifyItemChanged(newPos);
        });

        holder.btnAdd.setOnClickListener(v -> {
            int pos = holder.getBindingAdapterPosition();
            if (pos != RecyclerView.NO_POSITION && listener != null)
                listener.onAddClick(card, pos);
        });

        holder.btnInfo.setOnClickListener(v -> {
            int pos = holder.getBindingAdapterPosition();
            if (pos != RecyclerView.NO_POSITION && listener != null)
                listener.onInfoClick(card, pos);
        });
    }


    private float dp(View v, int dps) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dps, v.getResources().getDisplayMetrics());
    }

    @Override
    public int getItemCount() {
        return cards.size();
    }

    public void setCards(List<Card> newCards){
        this.cards.clear();
        this.cards.addAll(newCards);
        notifyDataSetChanged();
    }

    public void clearSelection() {
        if (openedPosition != RecyclerView.NO_POSITION) {
            int old = openedPosition;
            openedPosition = RecyclerView.NO_POSITION;
            notifyItemChanged(old);
        }
    }
}
