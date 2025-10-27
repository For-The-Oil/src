package io.github.shared.local.data.gameobject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

import io.github.shared.local.data.EnumsTypes.BuildingType;
import io.github.shared.local.data.EnumsTypes.DeckCardType;

public class Deck implements Serializable {
    private HashMap<DeckCardType, ArrayList<BuildingType>> cardTabKey;

    public Deck() {
        this.cardTabKey = new HashMap<DeckCardType, ArrayList<BuildingType>>();
    }

    public HashMap<DeckCardType, ArrayList<BuildingType>> getCardTabKey() {
        return cardTabKey;
    }

    public void setCardTabKey(HashMap<DeckCardType, ArrayList<BuildingType>> cardTabKey) {
        this.cardTabKey = cardTabKey;
    }
    public ArrayList<BuildingType> getCardArrayListKey(DeckCardType menu) {
        return cardTabKey.get(menu);
    }

    public BuildingType getCardKey(DeckCardType menu, int building) {
        return cardTabKey.get(menu).get(building);
    }

}
