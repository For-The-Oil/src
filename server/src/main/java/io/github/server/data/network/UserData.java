package io.github.server.data.network;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

import io.github.shared.data.enumsTypes.EntityType;
import io.github.shared.data.gameobject.Deck;


// Classe DTO pour regrouper toutes les infos
public class UserData {
    private UUID uuid;
    private String username;
    private HashMap<String, Deck> decks;
    private ArrayList<EntityType> unlockedCards;

    public UserData(UUID uuid, String username, HashMap<String, Deck> decks, ArrayList<EntityType> unlockedCards) {
        this.uuid = uuid;
        this.username = username;
        this.decks = decks;
        this.unlockedCards = unlockedCards;
    }

    public UUID getUuid() { return uuid; }
    public String getUsername() { return username; }
    public HashMap<String, Deck> getDecks() { return decks; }

    public ArrayList<EntityType> getUnlockedCards() {
        return unlockedCards;
    }
}

