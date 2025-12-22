package io.github.android.gui.fragment.game;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import io.github.android.utils.UiUtils;
import io.github.core.client_engine.manager.SessionManager;
import io.github.core.data.ClientGame;
import io.github.fortheoil.R;
import io.github.shared.data.enums_types.DeckCardCategory;
import io.github.shared.data.enums_types.EntityType;
import io.github.shared.data.gameobject.Deck;
import io.github.shared.data.network.Player;
import io.github.shared.shared_engine.Utility;

public abstract class BaseDeckFragment extends Fragment {

    protected RecyclerView recycler;
    protected BuildingAdapter adapter;
    protected abstract DeckCardCategory getCategory();
    protected static final String TAG = "BaseDeckFragment";

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        recycler = view.findViewById(R.id.recyclerSection1);
        recycler.setLayoutManager(new LinearLayoutManager(getContext()));

        loadCards();
    }

    private void loadCards() {
        Player current = Utility.findPlayerByUuid(
            ClientGame.getInstance().getPlayersList(),
            SessionManager.getInstance().getUuidClient()
        );

        if (current == null) {
            Log.d(TAG, "Aucun joueur trouvé !");
            return;
        }

        Deck deck = current.getCurrentDeck();
        if (deck == null) {
            Log.d(TAG, "Le joueur n'a pas de deck !");
            return;
        }

        List<EntityType> cards = deck.getCardsByCategory(deck, getCategory());
        Log.d(TAG, "Nombre de cartes " + getCategory() + " : " + cards.size());

        adapter = new BuildingAdapter(cards);
        recycler.setAdapter(adapter);
    }

    // ------------------ Adapter générique ------------------ //
    protected static class BuildingAdapter extends RecyclerView.Adapter<BuildingAdapter.BuildingViewHolder> {
        private final List<EntityType> cards;

        BuildingAdapter(List<EntityType> cards) { this.cards = cards; }

        @NonNull
        @Override
        public BuildingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            // Layout simple avec un ImageButton
            ImageButton itemButton = new ImageButton(parent.getContext());
            RecyclerView.LayoutParams lp = new RecyclerView.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                150 // hauteur fixe, tu peux ajuster
            );
            lp.setMargins(8, 8, 8, 8);
            itemButton.setLayoutParams(lp);
            itemButton.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            itemButton.setBackgroundColor(Color.TRANSPARENT); // pas de background
            return new BuildingViewHolder(itemButton);
        }

        @Override
        public void onBindViewHolder(@NonNull BuildingViewHolder holder, int position) {
            EntityType card = cards.get(position);
            int drawableId = UiUtils.mapEntityTypeToDrawable(card);
            holder.imageButton.setImageResource(drawableId);

            // Optionnel : click listener pour action sur la carte
            holder.imageButton.setOnClickListener(v -> {
                Log.d(TAG, "Carte cliquée : " + card.name());
                // TODO: actions éventuelles
            });
        }

        @Override
        public int getItemCount() { return cards.size(); }

        static class BuildingViewHolder extends RecyclerView.ViewHolder {
            ImageButton imageButton;

            BuildingViewHolder(@NonNull View itemView) {
                super(itemView);
                imageButton = (ImageButton) itemView;
            }
        }
    }

}
