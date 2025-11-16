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
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.flexbox.AlignItems;
import com.google.android.flexbox.FlexDirection;
import com.google.android.flexbox.FlexWrap;
import com.google.android.flexbox.FlexboxLayoutManager;
import com.google.android.flexbox.JustifyContent;

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

    private Button btnIndustry, btnMilitary, btnDefense;
    private RecyclerView deckRecycler, inventoryRecycler;
    private DeckAdapter deckAdapter;
    private SessionManager session;
    private Deck currentDeck;
    private DeckCardCategory activeCategory = DeckCardCategory.Military; // catégorie par défaut
    private View root;

    // Deck selector
    private LinearLayout deckSelectorLayout;
    private Button selectedDeckButton = null;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        root = inflater.inflate(R.layout.second_activity_deck, container, false);

        session = SessionManager.getInstance();

        initViews();
        loadDecksFromSession();
        initCategorySelector();
        initDeckRecycler();
        initInventoryRecycler();

        return root;
    }

    private void initViews() {
        btnIndustry = root.findViewById(R.id.btnIndustry);
        btnMilitary = root.findViewById(R.id.btnMilitary);
        btnDefense = root.findViewById(R.id.btnDefense);

        deckRecycler = root.findViewById(R.id.deckRecycler);
        inventoryRecycler = root.findViewById(R.id.inventoryRecycler);

        deckSelectorLayout = root.findViewById(R.id.deckSelectorLayout);
    }

    private void loadDecksFromSession() {
        if (session.getDecks() == null) return;

        for (String deckName : session.getDecks().keySet()) {
            Button deckButton = new Button(getContext());
            deckButton.setText(deckName);
            deckButton.setTextColor(getResources().getColor(android.R.color.white));
            deckButton.setBackgroundResource(R.drawable.button_deck);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            );
            params.setMargins(0, 0, 16, 0);
            deckButton.setLayoutParams(params);

            // Selection par défaut
            if (selectedDeckButton == null) {
                deckButton.setSelected(true);
                selectedDeckButton = deckButton;
                currentDeck = session.getDecks().get(deckName);
            }

            deckButton.setOnClickListener(v -> {
                if (selectedDeckButton != null) selectedDeckButton.setSelected(false);
                deckButton.setSelected(true);
                selectedDeckButton = deckButton;
                currentDeck = session.getDecks().get(deckName);
                updateDeckRecycler(currentDeck);
                updateInventoryRecycler();
            });

            deckSelectorLayout.addView(deckButton, deckSelectorLayout.getChildCount() - 1);
        }
    }

    private void initCategorySelector() {
        Button[] buttons = {btnIndustry, btnMilitary, btnDefense};
        DeckCardCategory[] categories = {DeckCardCategory.Industrial, DeckCardCategory.Military, DeckCardCategory.Defense};

        for (int i = 0; i < buttons.length; i++) {
            Button b = buttons[i];
            DeckCardCategory category = categories[i];

            b.setOnClickListener(v -> {
                activeCategory = category;
                updateCategorySelection(buttons, category);
                updateDeckRecycler(currentDeck);
                updateInventoryRecycler();
            });
        }

        updateCategorySelection(buttons, activeCategory);
    }

    private void updateCategorySelection(Button[] buttons, DeckCardCategory selectedCategory) {
        DeckCardCategory[] categories = {DeckCardCategory.Industrial, DeckCardCategory.Military, DeckCardCategory.Defense};

        for (int i = 0; i < buttons.length; i++) {
            buttons[i].setSelected(categories[i] == selectedCategory);
            buttons[i].setAlpha(categories[i] == selectedCategory ? 1f : 0.6f);
        }
    }

    private void initDeckRecycler() {
        FlexboxLayoutManager layoutManager = new FlexboxLayoutManager(getContext());
        layoutManager.setFlexDirection(FlexDirection.ROW);
        layoutManager.setFlexWrap(FlexWrap.WRAP);
        layoutManager.setJustifyContent(JustifyContent.FLEX_START);
        layoutManager.setAlignItems(AlignItems.FLEX_START);

        deckRecycler.setLayoutManager(layoutManager);

        deckAdapter = new DeckAdapter(convertDeckToCards(currentDeck, activeCategory), new DeckAdapter.OnCardActionListener() {
            @Override
            public void onAddClick(Card card, int position) {
                Toast.makeText(getContext(), "Ajouter " + card.getName(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onInfoClick(Card card, int position) {
                Toast.makeText(getContext(), "Info " + card.getName(), Toast.LENGTH_SHORT).show();
            }
        });

        deckRecycler.setAdapter(deckAdapter);
    }

    private void updateDeckRecycler(Deck deck) {
        if (deck == null || deckAdapter == null) return;

        List<Card> cards = convertDeckToCards(deck, activeCategory);
        deckAdapter.setCards(cards);
    }

    private void initInventoryRecycler() {
        FlexboxLayoutManager layoutManager = new FlexboxLayoutManager(getContext());
        layoutManager.setFlexDirection(FlexDirection.ROW);
        layoutManager.setFlexWrap(FlexWrap.WRAP);
        layoutManager.setJustifyContent(JustifyContent.FLEX_START);
        layoutManager.setAlignItems(AlignItems.FLEX_START);

        inventoryRecycler.setLayoutManager(layoutManager);

        updateInventoryRecycler();
    }

    private void updateInventoryRecycler() {
        List<Card> unlockedCardsUi = new ArrayList<>();
        if (session.getUnlockedCards() != null) {
            for (EntityType type : session.getUnlockedCards()) {
                if (type.getCategory() == activeCategory) {
                    unlockedCardsUi.add(new Card(UiUtils.mapEntityTypeToDrawable(type), type.name()));
                }
            }
        }

        if (inventoryRecycler.getAdapter() instanceof CardListAdapter) {
            ((CardListAdapter) inventoryRecycler.getAdapter()).setCards(unlockedCardsUi);
        } else {
            CardListAdapter adapter = new CardListAdapter(unlockedCardsUi, new CardListAdapter.OnCardActionListener() {
                @Override
                public void onAddClick(Card card, int position) {
                    Toast.makeText(getContext(), "Ajouter " + card.getName(), Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onInfoClick(Card card, int position) {
                    Toast.makeText(getContext(), "Info " + card.getName(), Toast.LENGTH_SHORT).show();
                }
            });
            inventoryRecycler.setAdapter(adapter);
        }
    }

    private List<Card> convertDeckToCards(Deck deck, DeckCardCategory category) {
        List<Card> cards = new ArrayList<>();
        if (deck == null) return cards;

        ArrayList<EntityType> types = deck.getCardArrayListKey(category);
        if (types != null) {
            for (EntityType t : types) {
                cards.add(new Card(UiUtils.mapEntityTypeToDrawable(t), t.name()));
            }
        }

        return cards;
    }

}
