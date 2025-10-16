package io.github.shared.local.data.gameobject;

import java.util.ArrayList;
import java.util.HashMap;

public class Deck {
    private HashMap<String,ArrayList<Integer>> cardTabKey;

    public Deck(HashMap<String, ArrayList<Integer>> cardTabKey) {
        this.cardTabKey = cardTabKey;
    }

    public HashMap<String, ArrayList<Integer>> getCardTabKey() {
        return cardTabKey;
    }

    public void setCardTabKey(HashMap<String, ArrayList<Integer>> cardTabKey) {
        this.cardTabKey = cardTabKey;
    }
    public ArrayList<Integer> getCardArrayListKey(String value) {
        return cardTabKey.get(value);
    }

    public Integer getCardKey(String menu, int building) {
        return cardTabKey.get(menu).get(building);
    }

}
