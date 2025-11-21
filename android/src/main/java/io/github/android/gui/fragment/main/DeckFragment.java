package io.github.android.gui.fragment.main;

import android.app.AlertDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
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
import java.util.Map;

import io.github.android.gui.Card;
import io.github.android.gui.adapter.CardListAdapter;
import io.github.android.gui.adapter.DeckAdapter;
import io.github.android.manager.DeckManager;
import io.github.android.manager.SessionManager;
import io.github.android.utils.UiUtils;
import io.github.fortheoil.R;
import io.github.shared.data.EnumsTypes.DeckCardCategory;
import io.github.shared.data.EnumsTypes.EntityType;
import io.github.shared.data.gameobject.Deck;


public class DeckFragment extends Fragment {

    private Button btnIndustry, btnMilitary, btnDefense;
    private TextView currentDeckName;
    private RecyclerView deckRecycler, inventoryRecycler;
    private DeckAdapter deckAdapter;
    private SessionManager session;
    private Deck currentDeck;
    private DeckCardCategory activeCategory = DeckCardCategory.Military;
    private View root;

    private LinearLayout deckSelectorLayout;
    private Button selectedDeckButton = null;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.second_activity_deck, container, false);
        session = SessionManager.getInstance();

        initViews();
        initCategorySelector();
        initDeckRecycler();
        initInventoryRecycler();
        refreshDeckButtons(); // met à jour les boutons dès le départ
        this.refreshUI();

        Button btnDeleteDeck = root.findViewById(R.id.btnDeleteDeck);
        if (btnDeleteDeck != null) {
            btnDeleteDeck.setOnClickListener(v -> onDeleteDeckClicked());
        }

        Button btnAddDeck = root.findViewById(R.id.btnAddDeck);
        if (btnAddDeck != null) {
            btnAddDeck.setOnClickListener(v -> onAddDeckClicked());
        }
        return root;
    }

    private void initViews() {
        btnIndustry = root.findViewById(R.id.btnIndustry);
        btnMilitary = root.findViewById(R.id.btnMilitary);
        btnDefense = root.findViewById(R.id.btnDefense);

        deckRecycler = root.findViewById(R.id.deckRecycler);
        inventoryRecycler = root.findViewById(R.id.inventoryRecycler);

        deckSelectorLayout = root.findViewById(R.id.deckSelectorLayout);
        currentDeckName = root.findViewById(R.id.currentDeckName);
    }

    private void initCategorySelector() {
        Button[] buttons = {btnIndustry, btnMilitary, btnDefense};
        DeckCardCategory[] categories = {DeckCardCategory.Industrial, DeckCardCategory.Military, DeckCardCategory.Defense};

        for (int i = 0; i < buttons.length; i++) {
            Button b = buttons[i];
            DeckCardCategory category = categories[i];
            b.setOnClickListener(v -> onCategorySelected(category));
        }

        updateCategorySelection(buttons, activeCategory);
    }

    private void onCategorySelected(DeckCardCategory category) {
        activeCategory = category;
        currentDeck = session.getCurrentDeck(); // toujours récupérer le deck courant
        Log.d("DeckFragment", "Category selected: " + category.name());
        updateCategorySelection(new Button[]{btnIndustry, btnMilitary, btnDefense}, category);
        updateDeckRecycler(currentDeck);
        updateInventoryRecycler();
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
                DeckManager.removeCard(card, currentDeckName.getText().toString(), activeCategory.name());
                refreshUI();
            }

            @Override
            public void onInfoClick(Card card, int position) {
                DeckManager.infoCard(getContext(), card);
            }
        });

        deckRecycler.setAdapter(deckAdapter);
    }

    private void updateDeckRecycler(Deck deck) {
        if (deck == null || deckAdapter == null) return;
        deckAdapter.setCards(convertDeckToCards(deck, activeCategory));
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
                    DeckManager.addCard(card, currentDeckName.getText().toString(), activeCategory.name());
                }

                @Override
                public void onInfoClick(Card card, int position) {
                    DeckManager.infoCard(getContext(), card);
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

    // ---- Refresh UI après update serveur ----
    public void refreshUI() {
        currentDeck = session.getCurrentDeck();
        Log.d("DeckFragment", "refreshUI() called, currentDeck=" + currentDeck);

        if (currentDeckName != null && currentDeck != null) {
            for (String deckName : session.getDecks().keySet()) {
                if (session.getDecks().get(deckName) == currentDeck) {
                    currentDeckName.setText(deckName);
                    break;
                }
            }
        }
        refreshDeckButtons();
        refreshCategoryListeners();
        updateDeckRecycler(currentDeck);
        updateInventoryRecycler();
    }

    private void refreshCategoryListeners() {
        Button[] buttons = {btnIndustry, btnMilitary, btnDefense};
        DeckCardCategory[] categories = {DeckCardCategory.Industrial, DeckCardCategory.Military, DeckCardCategory.Defense};

        for (int i = 0; i < buttons.length; i++) {
            Button b = buttons[i];
            DeckCardCategory category = categories[i];
            b.setOnClickListener(v -> onCategorySelected(category));
        }
        updateCategorySelection(buttons, activeCategory);
    }

    public void refreshDeckButtons() {
        if (deckSelectorLayout == null) return;

        deckSelectorLayout.removeAllViews();
        selectedDeckButton = null;

        Map<String, Deck> decks = session.getDecks();
        Deck currentDeck = session.getCurrentDeck();

        if (decks.isEmpty()) {
            currentDeckName.setText("Aucun deck");
            session.setCurrentDeck(null,null);
        } else {
            // Si currentDeck est null, on prend le premier deck
            if (currentDeck == null) {
                Map.Entry<String, Deck> firstEntry = decks.entrySet().iterator().next();
                currentDeck = firstEntry.getValue();
                session.setCurrentDeck(currentDeck,firstEntry.getKey());
                currentDeckName.setText(firstEntry.getKey());
            }
        }

        for (String deckName : decks.keySet()) {
            Button deckButton = new Button(getContext());
            deckButton.setText(deckName);
            deckButton.setTextColor(Color.WHITE);
            deckButton.setBackgroundResource(R.drawable.button_deck);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            );
            params.setMargins(0, 0, 16, 0);
            deckButton.setLayoutParams(params);

            // Sélection visuelle si c'est le deck courant
            if (currentDeck != null && decks.get(deckName) == currentDeck) {
                deckButton.setSelected(true);
                deckButton.setAlpha(1f);
                selectedDeckButton = deckButton;
            } else {
                deckButton.setSelected(false);
                deckButton.setAlpha(0.6f);
            }

            deckButton.setOnClickListener(v -> {
                if (selectedDeckButton != null) {
                    selectedDeckButton.setSelected(false);
                    selectedDeckButton.setAlpha(0.6f);
                }
                deckButton.setSelected(true);
                deckButton.setAlpha(1f);
                selectedDeckButton = deckButton;

                Deck selectedDeck = decks.get(deckName);
                session.setCurrentDeck(selectedDeck,deckName);
                currentDeckName.setText(deckName);

                updateDeckRecycler(selectedDeck);
                updateInventoryRecycler();
                DeckManager.selectDeck(deckName);
            });

            deckSelectorLayout.addView(deckButton);
        }

        // Bouton "+" pour ajouter un deck
        Button addButton = new Button(getContext());
        addButton.setText("+");
        addButton.setTextColor(Color.WHITE);
        addButton.setBackgroundResource(R.drawable.button_add);
        addButton.setOnClickListener(v -> onAddDeckClicked());

        deckSelectorLayout.addView(addButton);
    }



    // ---- Ajout / Suppression de decks ----
    private void onDeleteDeckClicked() {
        Deck current = session.getCurrentDeck();
        String name = findDeckName(current);

        if (current == null || name == null) {
            Toast.makeText(getContext(), "Aucun deck sélectionné", Toast.LENGTH_SHORT).show();
            return;
        }

        new AlertDialog.Builder(requireContext())
            .setTitle("Supprimer le deck")
            .setMessage("Voulez-vous vraiment supprimer \"" + name + "\" ?")
            .setPositiveButton("Oui", (dialog, which) -> DeckManager.deleteDeck(name))
            .setNegativeButton("Non", null)
            .show();
    }

    private String findDeckName(Deck deck) {
        if (deck == null) return null;
        for (Map.Entry<String, Deck> entry : session.getDecks().entrySet()) {
            if (entry.getValue() == deck) return entry.getKey();
        }
        return null;
    }

    private void onAddDeckClicked() {
        final EditText input = new EditText(requireContext());
        input.setHint("Nom du deck");
        input.setSingleLine(true);
        input.setPadding(32, 32, 32, 32);

        new AlertDialog.Builder(requireContext())
            .setTitle("Créer un nouveau deck")
            .setMessage("Entrez le nom du deck :")
            .setView(input)
            .setPositiveButton("Créer", (dialog, which) -> {
                String deckName = input.getText().toString().trim();
                if (deckName.isEmpty()) {
                    Toast.makeText(getContext(), "Le nom ne peut pas être vide", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (session.getDecks().containsKey(deckName)) {
                    Toast.makeText(getContext(), "Un deck avec ce nom existe déjà", Toast.LENGTH_SHORT).show();
                    return;
                }
                DeckManager.createDeck(deckName);
            })
            .setNegativeButton("Annuler", null)
            .show();
    }
}
