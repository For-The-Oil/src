package io.github.android.manager;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import io.github.shared.data.enumsTypes.EntityType;
import io.github.shared.data.gameobject.Deck;


/**
 * Method that build the current user session.
 * Save his connection token, username, decks...
 */
public class SessionManager {

    // Instance unique (singleton)
    private static SessionManager INSTANCE;

    // Données de session
    private String token;
    private String username;
    private Map<String, Deck> decks;
    private ArrayList<EntityType> unlockedCards;
    private Deck currentDeck;
    private String currentDeckName;
    private Boolean isActive=false;

    // Constructeur privé pour empêcher l’instanciation directe
    private SessionManager() {}

    // Accès global à l’instance unique
    public static synchronized SessionManager getInstance() {
        if (INSTANCE == null) INSTANCE = new SessionManager();
        return INSTANCE;
    }



    // Méthode utilitaire pour vider la session (logout)
    public void clearSession() {
        this.token = null;
        this.username = null;
        this.decks = null;
        this.isActive = false;
    }






    // --- Getters & Setters ---
    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Map<String, Deck> getDecks() {
        return decks;
    }

    public void setDecksFromJson(String decksJson) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            this.decks = mapper.readValue(decksJson, new TypeReference<Map<String, Deck>>() {});
        } catch (Exception e) {
            e.printStackTrace();
            this.decks = new HashMap<>();
        }
    }

    public void setUnlockedCardsFromJson(String unlockedsJson) {
        if (unlockedsJson == null || unlockedsJson.isEmpty()) {
            this.unlockedCards = new ArrayList<>();
            return;
        }

        try {
            ObjectMapper mapper = new ObjectMapper();
            this.unlockedCards = mapper.readValue(unlockedsJson, new TypeReference<ArrayList<EntityType>>() {});
        } catch (Exception e) {
            e.printStackTrace();
            this.unlockedCards = new ArrayList<>();
        }
    }


    public Boolean isActive() {
        return isActive;
    }

    public void setActive(Boolean active) {
        isActive = active;
    }

    public ArrayList<EntityType> getUnlockedCards() {
        return unlockedCards;
    }

    public Deck getCurrentDeck() {
        return currentDeck;
    }

    public void setCurrentDeck(Deck currentDeck, String currentDeckName) {
        this.currentDeck = currentDeck;
        this.currentDeckName = currentDeckName;
    }

    public String getCurrentDeckName() {
        return currentDeckName;
    }

    public void setCurrentDeckName(String currentDeckName) {
        this.currentDeckName = currentDeckName;
    }
}

