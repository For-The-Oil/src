package io.github.android.gui.adapter;

import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import io.github.android.gui.Card;
import io.github.fortheoil.R;

public class CardListAdapter extends RecyclerView.Adapter<CardListAdapter.CardViewHolder> {

    private final List<Card> cards;
    private int selectedPosition = RecyclerView.NO_POSITION;
    private final OnCardActionListener listener;

    // Interface pour gérer les actions
    public interface OnCardActionListener {
        void onAddClick(Card card, int position);
        void onInfoClick(Card card, int position);
    }

    public CardListAdapter(List<Card> cards, OnCardActionListener listener) {
        this.cards = cards;
        this.listener = listener;
    }

    // ViewHolder
    public static class CardViewHolder extends RecyclerView.ViewHolder {
        ImageView cardImage;
        LinearLayout cardActions;
        View btnAdd;
        View btnInfo;

        public CardViewHolder(@NonNull View itemView) {
            super(itemView);
            cardImage = itemView.findViewById(R.id.cardImage);
            cardActions = itemView.findViewById(R.id.cardActions);
            btnAdd = itemView.findViewById(R.id.btnAdd);
            btnInfo = itemView.findViewById(R.id.btnInfo);
        }
    }

    @NonNull
    @Override
    public CardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.item_card, parent, false);
        return new CardViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CardViewHolder holder, int position) {
        Card card = cards.get(position);
        holder.cardImage.setImageResource(card.getImageResId());

        boolean isSelected = (position == selectedPosition);

        ViewGroup.LayoutParams lp = holder.itemView.getLayoutParams();
        if (lp instanceof com.google.android.flexbox.FlexboxLayoutManager.LayoutParams) {
            com.google.android.flexbox.FlexboxLayoutManager.LayoutParams flexLp =
                (com.google.android.flexbox.FlexboxLayoutManager.LayoutParams) lp;

            flexLp.setFlexBasisPercent(0.3f);
            if (isSelected) {
                 // largeur 100% → une carte par ligne
                flexLp.height = (int) TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP,
                    200, // hauteur augmentée en dp
                    holder.itemView.getResources().getDisplayMetrics()
                );
            } else {
                flexLp.height = ViewGroup.LayoutParams.WRAP_CONTENT; // hauteur normale
            }
            holder.itemView.setLayoutParams(flexLp);
        }

        // Annule les anims en cours
        holder.cardImage.animate().cancel();
        holder.cardActions.animate().cancel();

        // État initial de l’overlay (caché dessous)
        if (!isSelected) {
            holder.cardImage.setScaleX(1f);
            holder.cardImage.setScaleY(1f);

            holder.cardActions.setAlpha(0f);
            holder.cardActions.setTranslationY(dp(holder.itemView, 16));
            holder.cardActions.setVisibility(View.GONE);
        } else {
            // Effet léger sur l’image
            holder.cardImage.animate()
                .scaleX(1.05f)
                .scaleY(1.05f)
                .setDuration(180)
                .start();

            // Afficher l’overlay sous la carte (slide up + fade-in)
            holder.cardActions.setVisibility(View.VISIBLE);
            holder.cardActions.setAlpha(0f);
            holder.cardActions.setTranslationY(dp(holder.itemView, 16));
            holder.cardActions.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(180)
                .start();
        }

        // Toggle sélection
        holder.itemView.setOnClickListener(v -> {
            int oldPosition = selectedPosition;
            int newPosition = holder.getAdapterPosition();
            if (newPosition == RecyclerView.NO_POSITION) return;

            selectedPosition = (selectedPosition == newPosition) ? RecyclerView.NO_POSITION : newPosition;
            notifyItemChanged(oldPosition);
            notifyItemChanged(selectedPosition);
        });

        // Boutons
        if (holder.btnAdd != null) {
            holder.btnAdd.setOnClickListener(v -> {
                int pos = holder.getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION && listener != null) {
                    listener.onAddClick(cards.get(pos), pos);
                }
            });
        }
        if (holder.btnInfo != null) {
            holder.btnInfo.setOnClickListener(v -> {
                int pos = holder.getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION && listener != null) {
                    listener.onInfoClick(cards.get(pos), pos);
                }
            });
        }

    }

    // Helper pour convertir dp -> px
    private float dp(View v, int dps) {
        return dps * v.getResources().getDisplayMetrics().density;
    }



    @Override
    public int getItemCount() {
        return cards.size();
    }
}
