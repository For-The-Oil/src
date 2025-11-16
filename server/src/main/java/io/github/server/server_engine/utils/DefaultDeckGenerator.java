package io.github.server.server_engine.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.shared.local.data.EnumsTypes.DeckCardCategory;
import io.github.shared.local.data.EnumsTypes.EntityType;
import io.github.shared.local.data.gameobject.Deck;

import java.util.*;

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
        military.add(EntityType.BASE);
        military.add(EntityType.GARAGE);
        military.add(EntityType.FACTORY);
        military.add(EntityType.BARRACK);

        defaultCards.put(DeckCardCategory.Military, military);
        defaultDeck.setCardTabKey(defaultCards);

        deckMap.put("Default Deck", defaultDeck);

        // ==============================================================
        // 2. STARTER DECK (Industrie + Défense légère)
        // ==============================================================
        Deck starterDeck = new Deck();
        HashMap<DeckCardCategory, ArrayList<EntityType>> starterCards = new HashMap<>();

        ArrayList<EntityType> industry = new ArrayList<>();
        industry.add(EntityType.BASE);
        industry.add(EntityType.FACTORY);

        ArrayList<EntityType> defense = new ArrayList<>();
        defense.add(EntityType.BARRACK);   // ex : infanterie légère
        defense.add(EntityType.GARAGE);    // ex : véhicules légers au départ

        starterCards.put(DeckCardCategory.Military, industry);
        starterCards.put(DeckCardCategory.Defense, defense);

        starterDeck.setCardTabKey(starterCards);

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
