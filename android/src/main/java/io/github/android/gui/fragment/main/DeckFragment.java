package io.github.android.gui.fragment.main;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.flexbox.FlexboxLayoutManager;
import com.google.android.flexbox.FlexDirection;
import com.google.android.flexbox.FlexWrap;
import com.google.android.flexbox.JustifyContent;
import com.google.android.flexbox.AlignItems;

import java.util.ArrayList;
import java.util.List;

import io.github.android.gui.Card;
import io.github.android.gui.adapter.CardListAdapter;
import io.github.android.gui.adapter.DeckAdapter;
import io.github.android.manager.SessionManager;
import io.github.android.utils.UiUtils;
import io.github.fortheoil.R;
import io.github.shared.local.data.EnumsTypes.DeckCardCategory;
import io.github.shared.local.data.EnumsTypes.EntityType;
import io.github.shared.local.data.gameobject.Deck;

public class DeckFragment extends Fragment {

    private LinearLayout deckSelectorLayout;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.second_activity_deck, container, false);

        // --- RecyclerView du deck ---
        RecyclerView deckRecycler = root.findViewById(R.id.deckRecycler);
        deckRecycler.setLayoutManager(new GridLayoutManager(getContext(), 3));

        // --- HorizontalScrollView des decks ---
        deckSelectorLayout = root.findViewById(R.id.deckSelectorLayout); // LinearLayout contenu dans le HorizontalScrollView
        Button btnAddDeck = root.findViewById(R.id.btnAddDeck);
        btnAddDeck.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Ajouter un nouveau deck", Toast.LENGTH_SHORT).show();
            // TODO: ouvrir interface création de deck
        });

        // --- Session et deck courant ---
        SessionManager session = SessionManager.getInstance();
        Deck currentDeck = null;
        if (session.getDecks() != null && session.getDecks().containsKey("Default Deck")) {
            currentDeck = session.getDecks().get("Default Deck");
        }

        // Afficher le deck courant
        updateDeckRecycler(deckRecycler, currentDeck);

        // --- Ajouter dynamiquement les boutons pour chaque deck ---
        if (session.getDecks() != null) {
            for (String deckName : session.getDecks().keySet()) {
                Button deckButton = new Button(getContext());
                deckButton.setText(deckName);
                deckButton.setTextColor(getResources().getColor(android.R.color.white));
                deckButton.setBackgroundResource(R.drawable.button_deck); // créer un drawable pour style
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                );
                params.setMargins(0, 0, 16, 0); // espace entre boutons
                deckButton.setLayoutParams(params);

                deckButton.setOnClickListener(v -> {
                    Deck selectedDeck = session.getDecks().get(deckName);
                    updateDeckRecycler(deckRecycler, selectedDeck);
                });

                // Ajouter le bouton avant le bouton "+"
                deckSelectorLayout.addView(deckButton, deckSelectorLayout.getChildCount() - 1);
            }
        }

        // --- RecyclerView de l’inventaire ---
        RecyclerView inventoryRecycler = root.findViewById(R.id.inventoryRecycler);
        FlexboxLayoutManager flexboxLayoutManager = new FlexboxLayoutManager(getContext());
        flexboxLayoutManager.setFlexDirection(FlexDirection.ROW);
        flexboxLayoutManager.setFlexWrap(FlexWrap.WRAP);
        flexboxLayoutManager.setJustifyContent(JustifyContent.FLEX_START);
        flexboxLayoutManager.setAlignItems(AlignItems.FLEX_START);
        inventoryRecycler.setLayoutManager(flexboxLayoutManager);

        // Récupérer les cartes débloquées
        List<Card> unlockedCards = new ArrayList<>();
        if (session.getUnlockedCards() != null) {
            for (EntityType type : session.getUnlockedCards()) {
                int resId = UiUtils.mapEntityTypeToDrawable(type);
                unlockedCards.add(new Card(resId, type.name()));
            }
        }

        CardListAdapter cardListAdapter = new CardListAdapter(unlockedCards, new CardListAdapter.OnCardActionListener() {
            @Override
            public void onAddClick(Card card, int position) {
                Toast.makeText(getContext(), "Ajouter " + card.getName(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onInfoClick(Card card, int position) {
                Toast.makeText(getContext(), "Info " + card.getName(), Toast.LENGTH_SHORT).show();
            }
        });
        inventoryRecycler.setAdapter(cardListAdapter);

        return root;
    }

    private void updateDeckRecycler(RecyclerView deckRecycler, Deck selectedDeck) {
        List<Integer> deckCards = new ArrayList<>();
        if (selectedDeck != null) {
            ArrayList<EntityType> militaryCards = selectedDeck.getCardArrayListKey(DeckCardCategory.Military);
            if (militaryCards != null) {
                for (EntityType type : militaryCards) {
                    int resId = UiUtils.mapEntityTypeToDrawable(type);
                    deckCards.add(resId);
                }
            }
        }

        DeckAdapter adapter = new DeckAdapter(deckCards, new DeckAdapter.OnCardClickListener() {
            @Override
            public void onAddClick(int position) {
                Toast.makeText(getContext(), "Ajouter carte du deck " + position, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onInfoClick(int position) {
                Toast.makeText(getContext(), "Info carte du deck " + position, Toast.LENGTH_SHORT).show();
            }
        });
        deckRecycler.setAdapter(adapter);
    }
}
