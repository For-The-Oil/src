package io.github.shared.local.data.network;

import io.github.shared.local.data.EnumsTypes.RessourcesType;
import io.github.shared.local.data.gameobject.Deck;

import java.util.*;

/**
 * Représente un joueur avec plusieurs Decks, bâtiments et unités.
 */
public class Player extends ClientNetwork {
    private ArrayList<Integer> buildingsKey;
    private ArrayList<Integer> unitsKey;
    private HashMap<RessourcesType, Integer> ressources;
    private Deck gameDeck;

    public Player(){
        super();
    }
    public Player(UUID uuid, String name, HashMap<String, Deck> fullDeck, Deck deck, String token) {
        super(uuid,name, fullDeck, token);
        this.gameDeck = deck;
        this.ressources = new HashMap<RessourcesType, Integer>();
        this.buildingsKey = new ArrayList<>();
        this.unitsKey = new ArrayList<>();
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
