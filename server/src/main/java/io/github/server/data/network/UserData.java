package io.github.server.data.network;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

import io.github.shared.data.enums_types.EntityType;
import io.github.shared.data.gameobject.Deck;


// Classe DTO pour regrouper toutes les infos
public class UserData {
    private UUID uuid;
    private String username;
    private String currentDeck;
    private HashMap<String, Deck> decks;
    private ArrayList<EntityType> unlockedCards;

    public UserData(UUID uuid, String username, HashMap<String, Deck> decks, ArrayList<EntityType> unlockedCards, String currentDeck) {
        this.uuid = uuid;
        this.username = username;
        this.decks = decks;
        this.unlockedCards = unlockedCards;
        this.currentDeck=currentDeck;
    }

    public UUID getUuid() { return uuid; }
    public String getUsername() { return username; }
    public HashMap<String, Deck> getDecks() { return decks; }

    public ArrayList<EntityType> getUnlockedCards() {
        return unlockedCards;
    }

    public String getCurrentDeck(){
        return currentDeck;
    }
}

