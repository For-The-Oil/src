package io.github.shared.data.gameobject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.github.shared.data.EnumsTypes.DeckCardCategory;
import io.github.shared.data.EnumsTypes.EntityType;

public class Deck implements Serializable {
    private HashMap<DeckCardCategory, ArrayList<EntityType>> cardsByCategory;

    public Deck() {
        this.cardsByCategory = new HashMap<DeckCardCategory, ArrayList<EntityType>>();
    }

    public HashMap<DeckCardCategory, ArrayList<EntityType>> getCardsByCategory() {
        return cardsByCategory;
    }

    public void setCardsByCategory(HashMap<DeckCardCategory, ArrayList<EntityType>> cardsByCategory) {
        this.cardsByCategory = cardsByCategory;
    }
    public ArrayList<EntityType> getCardArrayListKey(DeckCardCategory menu) {
        return cardsByCategory.get(menu);
    }

    public EntityType getCardKey(DeckCardCategory menu, int building) {
        return cardsByCategory.get(menu).get(building);
    }



    public List<EntityType> getCardsByCategory(Deck deck, DeckCardCategory category) {
        List<EntityType> list = new ArrayList<>();
        if (deck == null) return list;

        ArrayList<EntityType> categoryCards = deck.getCardArrayListKey(category);
        if (categoryCards != null) {
            list.addAll(categoryCards);
        }
        return list;
    }



    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Deck{");
        for (Map.Entry<DeckCardCategory, ArrayList<EntityType>> entry : cardsByCategory.entrySet()) {
            sb.append(entry.getKey()).append(" = ");
            sb.append(entry.getValue());
            sb.append(", ");
        }
        sb.append("}");
        return sb.toString();
    }

    public void addCard(EntityType card, DeckCardCategory category) {
        if (card == null || category == null) return;

        // Récupère la liste pour la catégorie, ou crée-la si elle n'existe pas
        ArrayList<EntityType> list = cardsByCategory.get(category);
        if (list == null) {
            list = new ArrayList<>();
            cardsByCategory.put(category, list);
        }

        // Évite les doublons
        if (!list.contains(card)) {
            list.add(card);
        }
    }

    public void removeCard(EntityType card, DeckCardCategory category) {
        if (card == null || category == null) return;

        ArrayList<EntityType> list = cardsByCategory.get(category);
        if (list != null) {
            list.remove(card);
        }
    }



}
