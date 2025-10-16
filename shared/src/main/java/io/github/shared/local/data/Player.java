package io.github.shared.local.data;

import io.github.shared.local.data.gameobject.Deck;

import java.util.*;

/**
 * Représente un joueur avec plusieurs Decks, bâtiments et unités.
 */
public class Player {

    private final String name;
    private final Deck deck;
    private final ArrayList<Integer> buildingsKey;
    private final ArrayList<Integer> unitsKey;

    public Player(String name,Deck deck) {
        this.name = name;
        this.deck = deck;
        this.buildingsKey = new ArrayList<>();
        this.unitsKey = new ArrayList<>();
    }

    public String getName() {
        return name;
    }
    public void addBuilding(int building) {
        buildingsKey.add(building);
    }

    public void removeBuilding(int building) {
        buildingsKey.remove(building);
    }

    public List<Integer> getBuildingsKey() {
        return buildingsKey;
    }

    public void addUnit(int unit) {
        unitsKey.add(unit);
    }

    public void removeUnit(int unit) {
        unitsKey.remove(unit);
    }

    public List<Integer> getUnitsKey() {
        return unitsKey;
    }
}
