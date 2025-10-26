package io.github.shared.local.data;

import io.github.shared.local.data.EnumsTypes.RessourcesType;
import io.github.shared.local.data.gameobject.Deck;

import java.io.Serializable;
import java.util.*;

/**
 * Représente un joueur avec plusieurs Decks, bâtiments et unités.
 */
public class Player {

    private String UUID;
    private String name;
    private Deck deck;
    private ArrayList<Integer> buildingsKey;
    private ArrayList<Integer> unitsKey;
    private HashMap<RessourcesType, Integer> ressources;

    public Player(){}
    public Player(String uuid, String name, Deck deck) {
        this.UUID = uuid;
        this.name = name;
        this.deck = deck;
        this.ressources = new HashMap<RessourcesType, Integer>();
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
