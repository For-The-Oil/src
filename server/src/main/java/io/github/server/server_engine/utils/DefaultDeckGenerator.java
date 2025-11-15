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
        Deck defaultDeck = new Deck();

        // Exemple : remplir les catégories avec quelques bâtiments / unités de départ
        HashMap<DeckCardCategory, ArrayList<EntityType>> cardMap = new HashMap<>();

        // Pour la catégorie principale, par exemple Bâtiments
        ArrayList<EntityType> buildingList = new ArrayList<>();
        buildingList.add(EntityType.BASE);
        buildingList.add(EntityType.GARAGE);
        buildingList.add(EntityType.FACTORY);
        buildingList.add(EntityType.BARRACK);

        cardMap.put(DeckCardCategory.Military, buildingList);

        defaultDeck.setCardTabKey(cardMap);

        Map<String, Deck> deckMap = new HashMap<>();
        deckMap.put("Default Deck", defaultDeck);

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
