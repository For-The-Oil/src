package io.github.server.server_engine.utils;

import com.fasterxml.jackson.databind.ObjectMapper;


import java.util.*;

import io.github.shared.data.EnumsTypes.DeckCardCategory;
import io.github.shared.data.EnumsTypes.EntityType;
import io.github.shared.data.gameobject.Deck;

public final class DefaultDeckGenerator {

    private static final ObjectMapper mapper = new ObjectMapper();

    private DefaultDeckGenerator() {
        // Classe utilitaire
    }

    /**
     * Génère un deck par défaut pour un nouvel utilisateur.
     * @return Map du nom du deck vers l'objet Deck
     */
    public static Map<String, Deck> generateDefaultDeck() {

        Map<String, Deck> deckMap = new HashMap<>();

        // ==============================================================
        // 1. DEFAULT DECK (Deck principal militaire)
        // ==============================================================
        Deck defaultDeck = new Deck();
        HashMap<DeckCardCategory, ArrayList<EntityType>> defaultCards = new HashMap<>();

        ArrayList<EntityType> military = new ArrayList<>();
        military.add(EntityType.GARAGE);
        military.add(EntityType.FACTORY);
        military.add(EntityType.BARRACK);

        ArrayList<EntityType> industrial = new ArrayList<>();
        industrial.add(EntityType.BASE);

        ArrayList<EntityType> defense = new ArrayList<>();

        defaultCards.put(DeckCardCategory.Military, military);
        defaultCards.put(DeckCardCategory.Industrial, industrial);
        defaultCards.put(DeckCardCategory.Defense, defense);
        defaultDeck.setCardsByCategory(defaultCards);

        deckMap.put("Default Deck", defaultDeck);

        // ==============================================================
        // 2. STARTER DECK (Industrie + Défense légère)
        // ==============================================================
        Deck starterDeck = new Deck();
        HashMap<DeckCardCategory, ArrayList<EntityType>> starterCards = new HashMap<>();

        ArrayList<EntityType> military2 = new ArrayList<>();
        military2.add(EntityType.GARAGE);

        ArrayList<EntityType> industrial2 = new ArrayList<>();
        industrial2.add(EntityType.BASE);

        ArrayList<EntityType> defense2 = new ArrayList<>();

        starterCards.put(DeckCardCategory.Military, military2);
        starterCards.put(DeckCardCategory.Industrial, industrial2);
        starterCards.put(DeckCardCategory.Defense, defense2);
        starterDeck.setCardsByCategory(starterCards);

        deckMap.put("Starter Deck", starterDeck);

        return deckMap;
    }


    /**
     * Sérialise le deck par défaut en JSON utilisable pour la DB.
     * @return JSON string
     */
    public static String generateDefaultDeckJson() {
        try {
            return mapper.writeValueAsString(generateDefaultDeck());
        } catch (Exception e) {
            e.printStackTrace();
            return "{}"; // fallback JSON vide
        }
    }
}
