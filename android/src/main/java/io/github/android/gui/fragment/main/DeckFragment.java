package io.github.android.gui.fragment.main;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.flexbox.AlignItems;
import com.google.android.flexbox.FlexDirection;
import com.google.android.flexbox.FlexWrap;
import com.google.android.flexbox.FlexboxLayoutManager;
import com.google.android.flexbox.JustifyContent;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import io.github.android.gui.Card;
import io.github.android.gui.adapter.CardListAdapter;
import io.github.android.gui.adapter.DeckAdapter;
import io.github.android.manager.DeckManager;
import io.github.core.client_engine.manager.SessionManager;
import io.github.android.utils.UiUtils;
import io.github.fortheoil.R;
import io.github.shared.data.enums_types.DeckCardCategory;
import io.github.shared.data.enums_types.EntityType;
import io.github.shared.data.gameobject.Deck;

public class DeckFragment extends Fragment {

    // ---- UI Elements ----
    private Button btnIndustry, btnMilitary, btnDefense;
    private TextView currentDeckName;
    private RecyclerView deckRecycler, inventoryRecycler;
    private LinearLayout deckSelectorLayout;
    private Button selectedDeckButton = null;

    // ---- Adapters & Data ----
    private DeckAdapter deckAdapter;
    private CardListAdapter cardListAdapter;
    private SessionManager session;
    private Deck currentDeck;
    private DeckCardCategory activeCategory = DeckCardCategory.Military;

    // ---- Helpers ----
    private View root;
    private java.util.function.Predicate<MotionEvent> clickedInCardActions;

    // =====================================================================================
    // ---- onCreateView : initialisation principale ----
    // =====================================================================================
    @SuppressLint("ClickableViewAccessibility")
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.second_activity_deck, container, false);
        session = SessionManager.getInstance();

        // --- Initialisation vues ---
        initViews();
        initCategorySelector();
        initDeckRecycler();
        initInventoryRecycler();
        initSort();

        // --- Deck buttons & refresh ---
        refreshDeckButtons();
        refreshUI();

        // --- Add/Delete deck buttons ---
        setupAddDeleteButtons();

        // --- Gestion touch pour clear selection ---
        setupGlobalTouchListeners();

        // --- Recycler touch listeners ---
        inventoryRecycler.addOnItemTouchListener(makeTouchListener(inventoryRecycler));
        deckRecycler.addOnItemTouchListener(makeTouchListener(deckRecycler));

        return root;
    }

    // =====================================================================================
    // ---- Initialisation des vues ----
    // =====================================================================================
    private void initViews() {
        btnIndustry = root.findViewById(R.id.btnIndustry);
        btnMilitary = root.findViewById(R.id.btnMilitary);
        btnDefense = root.findViewById(R.id.btnDefense);

        deckRecycler = root.findViewById(R.id.deckRecycler);
        inventoryRecycler = root.findViewById(R.id.inventoryRecycler);

        deckSelectorLayout = root.findViewById(R.id.deckSelectorLayout);
        currentDeckName = root.findViewById(R.id.currentDeckName);
    }

    // =====================================================================================
    // ---- Catégorie ----
    // =====================================================================================
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
        currentDeck = session.getCurrentDeck();
        Log.d("DeckFragment", "Category selected: " + category.name());

        updateCategorySelection(new Button[]{btnIndustry, btnMilitary, btnDefense}, category);
        updateDeckRecycler(currentDeck);
        updateInventoryRecycler();

        if (deckAdapter != null) deckAdapter.clearSelection();
        if (cardListAdapter != null) cardListAdapter.clearSelection();
    }

    private void updateCategorySelection(Button[] buttons, DeckCardCategory selectedCategory) {
        DeckCardCategory[] categories = {DeckCardCategory.Industrial, DeckCardCategory.Military, DeckCardCategory.Defense};
        for (int i = 0; i < buttons.length; i++) {
            buttons[i].setSelected(categories[i] == selectedCategory);
            buttons[i].setAlpha(categories[i] == selectedCategory ? 1f : 0.6f);
        }
    }

    // =====================================================================================
    // ---- Deck Recycler ----
    // =====================================================================================
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
                clearAdapterSelection(inventoryRecycler);
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
        if (deckAdapter == null) return;
        if (deck == null) deckAdapter.setCards(new ArrayList<>());
        else deckAdapter.setCards(convertDeckToCards(deck, activeCategory));
    }

    // =====================================================================================
    // ---- Inventory Recycler ----
    // =====================================================================================
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
                    unlockedCardsUi.add(new Card(type, UiUtils.mapEntityTypeToDrawable(type)));
                }
            }
        }

        if (inventoryRecycler.getAdapter() instanceof CardListAdapter) {
            ((CardListAdapter) inventoryRecycler.getAdapter()).setCards(unlockedCardsUi);
        } else {
            cardListAdapter = new CardListAdapter(unlockedCardsUi, new CardListAdapter.OnCardActionListener() {
                @Override
                public void onAddClick(Card card, int position) {
                    clearAdapterSelection(deckRecycler);
                    DeckManager.addCard(card, currentDeckName.getText().toString(), activeCategory.name());
                }

                @Override
                public void onInfoClick(Card card, int position) {
                    DeckManager.infoCard(getContext(), card);
                }
            });
            inventoryRecycler.setAdapter(cardListAdapter);
        }
    }

    private List<Card> convertDeckToCards(Deck deck, DeckCardCategory category) {
        List<Card> cards = new ArrayList<>();
        if (deck == null) return cards;

        ArrayList<EntityType> types = deck.getCardArrayListKey(category);
        if (types != null) {
            for (EntityType t : types) {
                cards.add(new Card(t, UiUtils.mapEntityTypeToDrawable(t)));
            }
        }
        return cards;
    }

    // =====================================================================================
    // ---- Gestion Deck Buttons ----
    // =====================================================================================
    public void refreshDeckButtons() {
        if (deckSelectorLayout == null) return;

        deckSelectorLayout.removeAllViews();
        selectedDeckButton = null;

        Map<String, Deck> decks = session.getDecks();
        Deck currentDeck = session.getCurrentDeck();

        if (decks.isEmpty()) {
            currentDeckName.setText("Aucun deck");
            session.setCurrentDeck(null,null);
        } else if (currentDeck == null) {
            Map.Entry<String, Deck> firstEntry = decks.entrySet().iterator().next();
            currentDeck = firstEntry.getValue();
            session.setCurrentDeck(currentDeck,firstEntry.getKey());
            currentDeckName.setText(firstEntry.getKey());
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

            // sélection visuelle
            if (decks.get(deckName) == currentDeck) {
                deckButton.setSelected(true);
                deckButton.setAlpha(1f);
                selectedDeckButton = deckButton;
            } else {
                deckButton.setSelected(false);
                deckButton.setAlpha(0.6f);
            }

            deckButton.setOnClickListener(v -> selectDeck(deckName));
            deckSelectorLayout.addView(deckButton);
        }

        // Bouton + pour ajouter un deck
        LayoutInflater inflater = LayoutInflater.from(getContext());
        MaterialButton addButton = (MaterialButton) inflater.inflate(R.layout.add_deck_btn, deckSelectorLayout, false);

        addButton.setOnClickListener(v -> onAddDeckClicked());

        deckSelectorLayout.addView(addButton);
    }

    private void selectDeck(String deckName) {
        if (selectedDeckButton != null) {
            selectedDeckButton.setSelected(false);
            selectedDeckButton.setAlpha(0.6f);
        }

        Button deckButton = findButtonByName(deckName); // TODO: implémenter si nécessaire
        if (deckButton != null) {
            deckButton.setSelected(true);
            deckButton.setAlpha(1f);
            selectedDeckButton = deckButton;
        }

        Deck selectedDeck = session.getDecks().get(deckName);
        session.setCurrentDeck(selectedDeck, deckName);
        currentDeckName.setText(deckName);

        updateDeckRecycler(selectedDeck);
        updateInventoryRecycler();
        DeckManager.selectDeck(deckName);

        if (deckAdapter != null) deckAdapter.clearSelection();
        if (cardListAdapter != null) cardListAdapter.clearSelection();
    }

    // =====================================================================================
    // ---- Gestion Add/Delete Deck ----
    // =====================================================================================
    private void setupAddDeleteButtons() {
        Button btnDeleteDeck = root.findViewById(R.id.btnDeleteDeck);
        if (btnDeleteDeck != null) btnDeleteDeck.setOnClickListener(v -> onDeleteDeckClicked());

        Button btnAddDeck = root.findViewById(R.id.btnAddDeck);
        if (btnAddDeck != null) btnAddDeck.setOnClickListener(v -> onAddDeckClicked());
    }

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

    private String findDeckName(Deck deck) {
        if (deck == null) return null;
        for (Map.Entry<String, Deck> entry : session.getDecks().entrySet()) {
            if (entry.getValue() == deck) return entry.getKey();
        }
        return null;
    }

    // =====================================================================================
    // ---- Global Touch & Card Actions ----
    // =====================================================================================
    private void setupGlobalTouchListeners() {
        // initialisation clickedInCardActions
        java.util.function.BiFunction<View, MotionEvent, Boolean> isInsideView = (view, ev) -> {
            if (view == null) return false;
            int[] loc = new int[2];
            view.getLocationOnScreen(loc);
            float x = ev.getRawX();
            float y = ev.getRawY();
            return x >= loc[0] && x <= loc[0] + view.getWidth()
                && y >= loc[1] && y <= loc[1] + view.getHeight();
        };

        View sortMenu = root.findViewById(R.id.sortMenu);
        View btnSort = root.findViewById(R.id.btnSort);


        clickedInCardActions = ev -> {
            RecyclerView[] recyclers = {inventoryRecycler, deckRecycler};

            for (RecyclerView recycler : recyclers) {
                RecyclerView.Adapter<?> adapter = recycler.getAdapter();
                for (int i = 0; i < recycler.getChildCount(); i++) {
                    View itemView = recycler.getChildAt(i);
                    View cardContainer = itemView.findViewById(R.id.cardContainer);
                    View cardActions = itemView.findViewById(R.id.cardActions);

                    if ((cardActions != null && isInsideView.apply(cardActions, ev)) ||
                        (cardContainer != null && isInsideView.apply(cardContainer, ev)) ||
                        isInsideView.apply(itemView, ev)) {

                        // On ferme toutes les autres cartes sauf celles du recyclerview actuel
                        for (RecyclerView otherRecycler : recyclers) {
                            if (otherRecycler != recycler) {
                                clearAdapterSelection(otherRecycler);
                            }
                        }

                        return true; // on est "dans la carte", donc ne rien fermer dans ce recyclerview
                    }
                }
            }

            // Ignorer clics sur menu de tri ou bouton
            if (isInsideView.apply(sortMenu, ev) || isInsideView.apply(btnSort, ev)) {
                return true;
            }

            // clic en dehors → fermer tout
            for (RecyclerView recycler : recyclers) {
                clearAdapterSelection(recycler);
            }
            return false;
        };




        // TouchListener global pour clear selection
        Runnable clearSelectionIfNotInActions = () -> {
            clearAdapterSelection(inventoryRecycler);
            clearAdapterSelection(deckRecycler);
        };

        NestedScrollView nested = root.findViewById(R.id.nestedView);
        HorizontalScrollView deckSelector = root.findViewById(R.id.deckSelector);

        View.OnTouchListener containerTouch = (v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN && !clickedInCardActions.test(event)) {
                clearSelectionIfNotInActions.run();
            }
            if (event.getAction() == MotionEvent.ACTION_UP) v.performClick();
            return false;
        };

        nested.setOnTouchListener(containerTouch);
        deckSelector.setOnTouchListener(containerTouch);
    }

    private void clearAdapterSelection(RecyclerView recycler) {
        RecyclerView.Adapter<?> adapter = recycler.getAdapter();
        if (adapter instanceof CardListAdapter) ((CardListAdapter) adapter).clearSelection();
        else if (adapter instanceof DeckAdapter) ((DeckAdapter) adapter).clearSelection();
    }

    private RecyclerView.OnItemTouchListener makeTouchListener(RecyclerView recycler) {
        return new RecyclerView.OnItemTouchListener() {
            final GestureDetector gd = new GestureDetector(getContext(),
                new GestureDetector.SimpleOnGestureListener() {
                    @Override
                    public boolean onSingleTapUp(MotionEvent e) {
                        if (!clickedInCardActions.test(e)) {
                            View child = recycler.findChildViewUnder(e.getX(), e.getY());
                            if (child == null) clearAdapterSelection(recycler);
                        }
                        return false;
                    }
                });

            @Override
            public boolean onInterceptTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {
                gd.onTouchEvent(e);
                return false;
            }

            @Override public void onTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {}
            @Override public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {}
        };
    }

    private void closeAllCardActionsExcept(View except) {
        RecyclerView[] recyclers = {inventoryRecycler, deckRecycler};
        for (RecyclerView recycler : recyclers) {
            for (int i = 0; i < recycler.getChildCount(); i++) {
                View child = recycler.getChildAt(i);
                View actions = child.findViewById(R.id.cardActions);
                if (actions != null && actions != except) actions.setVisibility(View.GONE);
            }
        }
    }

    /**
     * Recherche un Button dans deckSelectorLayout correspondant au nom du deck
     */
    private Button findButtonByName(String deckName) {
        if (deckSelectorLayout == null) return null;

        for (int i = 0; i < deckSelectorLayout.getChildCount(); i++) {
            View child = deckSelectorLayout.getChildAt(i);
            if (child instanceof Button) {
                Button b = (Button) child;
                if (deckName.equals(b.getText().toString())) {
                    return b;
                }
            }
        }
        return null; // pas trouvé
    }

    // =====================================================================================
    // ---- Refresh UI ----
    // =====================================================================================
    public void refreshUI() {
        currentDeck = session.getCurrentDeck();
        Log.d("DeckFragment", "refreshUI() called, currentDeck=" + currentDeck);

        if (currentDeckName != null && currentDeck != null) {
            for (Map.Entry<String, Deck> entry : session.getDecks().entrySet()) {
                if (entry.getValue() == currentDeck) {
                    currentDeckName.setText(entry.getKey());
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



    //------
    // Sort menu
    //------
    private void initSort() {
        MaterialButton btnSort = root.findViewById(R.id.btnSort);
        LinearLayout sortMenu = root.findViewById(R.id.sortMenu);

        TextView sortByName = root.findViewById(R.id.sortByName);
        TextView sortByCategory = root.findViewById(R.id.sortByCategory);
        TextView sortByMaxHealth = root.findViewById(R.id.sortByMaxHealth);
        TextView sortByArmor = root.findViewById(R.id.sortByArmor);
        TextView sortByTotalCost = root.findViewById(R.id.sortByTotalCost);

        btnSort.setOnClickListener(v -> {
            sortMenu.setVisibility(sortMenu.getVisibility() == View.GONE ? View.VISIBLE : View.GONE);
        });

        sortByName.setOnClickListener(v -> {
            if (cardListAdapter != null) cardListAdapter.sortByName();
            sortMenu.setVisibility(View.GONE);
            btnSort.setText("Trier: Nom ▼");
        });

        sortByCategory.setOnClickListener(v -> {
            if (cardListAdapter != null) cardListAdapter.sortByCategory();
            sortMenu.setVisibility(View.GONE);
            btnSort.setText("Trier: Catégorie ▼");
        });

        sortByMaxHealth.setOnClickListener(v -> {
            if (cardListAdapter != null) cardListAdapter.sortByMaxHealth();
            sortMenu.setVisibility(View.GONE);
            btnSort.setText("Trier: Santé ▼");
        });

        sortByArmor.setOnClickListener(v -> {
            if (cardListAdapter != null) cardListAdapter.sortByArmor();
            sortMenu.setVisibility(View.GONE);
            btnSort.setText("Trier: Armure ▼");
        });

        sortByTotalCost.setOnClickListener(v -> {
            if (cardListAdapter != null) cardListAdapter.sortByTotalCost();
            sortMenu.setVisibility(View.GONE);
            btnSort.setText("Trier: Coût total ▼");
        });
    }


}
