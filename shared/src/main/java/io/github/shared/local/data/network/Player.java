package io.github.shared.local.data.network;

import io.github.shared.local.data.EnumsTypes.EntityType;
import io.github.shared.local.data.EnumsTypes.RessourcesType;
import io.github.shared.local.data.gameobject.Deck;

import java.util.*;

/**
 * Représente un joueur avec plusieurs Decks, bâtiments et unités.
 */
public class Player extends ClientNetwork {
    private HashMap<RessourcesType, Integer> resources;
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
    public HashMap<RessourcesType, Integer> getResources() {
        return resources;
    }

    public void setResources(HashMap<RessourcesType, Integer> resources) {
        this.resources = resources;
    }

    public Deck getGameDeck() {
        return gameDeck;
    }

    public void setGameDeck(Deck gameDeck) {
        this.gameDeck = gameDeck;
    }
}
