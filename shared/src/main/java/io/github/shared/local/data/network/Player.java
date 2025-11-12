package io.github.shared.local.data.network;

import io.github.shared.local.data.EnumsTypes.RessourcesType;
import io.github.shared.local.data.gameobject.Deck;

import java.util.*;

/**
 * Représente un joueur avec plusieurs Decks, bâtiments et unités.
 */
public class Player extends ClientNetwork {
    private ArrayList<Integer> buildingsNetId;
    private ArrayList<Integer> unitsNetId;
    private HashMap<RessourcesType, Integer> ressources;
    private Deck gameDeck;

    public Player(){
        super();
    }
    public Player(UUID uuid, String name, HashMap<String, Deck> fullDeck, Deck deck, String token) {
        super(uuid,name, fullDeck, token);
        this.gameDeck = deck;
        this.ressources = new HashMap<RessourcesType, Integer>();
        this.buildingsNetId = new ArrayList<>();
        this.unitsNetId = new ArrayList<>();
    }
    public void addBuilding(int building) {
        buildingsNetId.add(building);
    }

    public void removeBuilding(int building) {
        buildingsNetId.remove(building);
    }

    public List<Integer> getBuildingsNetId() {
        return buildingsNetId;
    }

    public void addUnit(int unit) {
        unitsNetId.add(unit);
    }

    public void removeUnit(int unit) {
        unitsNetId.remove(unit);
    }

    public List<Integer> getUnitsNetId() {
        return unitsNetId;
    }

    public HashMap<RessourcesType, Integer> getRessources() {
        return ressources;
    }

    public void setRessources(HashMap<RessourcesType, Integer> ressources) {
        this.ressources = ressources;
    }
}
