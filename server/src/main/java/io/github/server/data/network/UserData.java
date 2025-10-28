package io.github.server.data.network;

import java.util.HashMap;
import java.util.UUID;

import io.github.shared.local.data.gameobject.Deck;

// Classe DTO pour regrouper toutes les infos
public class UserData {
    private UUID uuid;
    private String username;
    private HashMap<String, Deck> decks;

    public UserData(UUID uuid, String username, HashMap<String, Deck> decks) {
        this.uuid = uuid;
        this.username = username;
        this.decks = decks;
    }

    public UUID getUuid() { return uuid; }
    public String getUsername() { return username; }
    public HashMap<String, Deck> getDecks() { return decks; }
}

