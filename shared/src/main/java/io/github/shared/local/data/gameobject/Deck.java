package io.github.shared.local.data.gameobject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.github.shared.local.data.EnumsTypes.DeckCardCategory;
import io.github.shared.local.data.EnumsTypes.EntityType;

public class Deck implements Serializable {
    private HashMap<DeckCardCategory, ArrayList<EntityType>> cardTabKey;

    public Deck() {
        this.cardTabKey = new HashMap<DeckCardCategory, ArrayList<EntityType>>();
    }

    public HashMap<DeckCardCategory, ArrayList<EntityType>> getCardTabKey() {
        return cardTabKey;
    }

    public void setCardTabKey(HashMap<DeckCardCategory, ArrayList<EntityType>> cardTabKey) {
        this.cardTabKey = cardTabKey;
    }
    public ArrayList<EntityType> getCardArrayListKey(DeckCardCategory menu) {
        return cardTabKey.get(menu);
    }

    public EntityType getCardKey(DeckCardCategory menu, int building) {
        return cardTabKey.get(menu).get(building);
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
        for (Map.Entry<DeckCardCategory, ArrayList<EntityType>> entry : cardTabKey.entrySet()) {
            sb.append(entry.getKey()).append(" = ");
            sb.append(entry.getValue());
            sb.append(", ");
        }
        sb.append("}");
        return sb.toString();
    }




}
