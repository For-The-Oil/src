package io.github.android.manager;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.HashMap;
import java.util.Map;

public class SessionManager {

    // Instance unique (singleton)
    private static SessionManager INSTANCE;

    // Données de session
    private String token;
    private String username;
    private Map<String, Object> decks;
    private Boolean isActive=false;

    // Constructeur privé pour empêcher l’instanciation directe
    private SessionManager() {}

    // Accès global à l’instance unique
    public static synchronized SessionManager getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new SessionManager();
        }
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

    public Map<String, Object> getDecks() { return decks; }

    public void setDecksFromJson(String decksJson) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            this.decks = mapper.readValue(decksJson, new TypeReference<Map<String, Object>>() {});
        } catch (Exception e) {
            e.printStackTrace();
            this.decks = new HashMap<>();
        }
    }

    public Boolean isActive() {
        return isActive;
    }

    public void setActive(Boolean active) {
        isActive = active;
    }
}

