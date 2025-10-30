package io.github.shared.local.data.gameobject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

import io.github.shared.local.data.EnumsTypes.DeckCardType;
import io.github.shared.local.data.EnumsTypes.EntityType;

public class Deck implements Serializable {
    private HashMap<DeckCardType, ArrayList<EntityType>> cardTabKey;

    public Deck() {
        this.cardTabKey = new HashMap<DeckCardType, ArrayList<EntityType>>();
    }

    public HashMap<DeckCardType, ArrayList<EntityType>> getCardTabKey() {
        return cardTabKey;
    }

    public void setCardTabKey(HashMap<DeckCardType, ArrayList<EntityType>> cardTabKey) {
        this.cardTabKey = cardTabKey;
    }
    public ArrayList<EntityType> getCardArrayListKey(DeckCardType menu) {
        return cardTabKey.get(menu);
    }

    public EntityType getCardKey(DeckCardType menu, int building) {
        return cardTabKey.get(menu).get(building);
    }

}
