package io.github.shared.local.data.gameobject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

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

}
