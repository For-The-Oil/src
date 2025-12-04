package io.github.shared.data.network;

import io.github.shared.data.enums_types.ResourcesType;
import io.github.shared.data.gameobject.Deck;

import java.util.*;

/**
 * Représente un joueur avec plusieurs Decks, bâtiments et unités.
 */
public class Player extends ClientNetwork {
    private HashMap<ResourcesType, Integer> resources;
    private Deck gameDeck;

    public Player(){
        super();
    }
    public Player(UUID uuid, String name, HashMap<String, Deck> fullDeck, Deck deck, String token) {
        super(uuid,name, fullDeck, null, token);
        this.gameDeck = deck;
        this.resources = new HashMap<>();
    }
    public Player(ClientNetwork cn){
        this(cn.getUuid(), cn.getUsername(), cn.getDecks(), cn.getCurrentDeck(), cn.getToken());
    }
    public HashMap<ResourcesType, Integer> getResources() {
        return resources;
    }

    public void setResources(HashMap<ResourcesType, Integer> resources) {
        this.resources = resources;
    }

    public Deck getGameDeck() {
        return gameDeck;
    }

    public void setGameDeck(Deck gameDeck) {
        this.gameDeck = gameDeck;
    }
}
