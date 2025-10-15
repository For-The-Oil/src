package io.github.shared.local.data;

import io.github.shared.local.data.gameobject.Building;
import io.github.shared.local.data.gameobject.Deck;
import io.github.shared.local.data.gameobject.Unit;

import java.util.*;

/**
 * Représente un joueur avec plusieurs Decks, bâtiments et unités.
 */
public class Player {

    private final String name;  // nom du joueur
    private final Map<String, Deck> decks; // Decks par catégorie
    private final List<Building> buildings; // tous les bâtiments possédés
    private final List<Unit> units;         // toutes les unités possédées

    public Player(String name) {
        this.name = name;
        this.decks = new HashMap<>();
        this.buildings = new ArrayList<>();
        this.units = new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    /** Ajoute un Deck pour une catégorie spécifique */
    public void addDeck(String category, Deck deck) {
        decks.put(category, deck);
    }

    /** Retourne le Deck d'une catégorie */
    public Deck getDeck(String category) {
        return decks.get(category);
    }

    /** Vérifie si le joueur possède un Deck pour une catégorie */
    public boolean hasDeck(String category) {
        return decks.containsKey(category);
    }

    /** Retourne toutes les catégories possédées par le joueur */
    public Map<String, Deck> getAllDecks() {
        return new HashMap<>(decks); // copie pour éviter modification externe
    }

    /** Ajoute un bâtiment à la liste des bâtiments possédés */
    public void addBuilding(Building building) {
        buildings.add(building);
    }

    /** Retourne tous les bâtiments possédés */
    public List<Building> getBuildings() {
        return new ArrayList<>(buildings); // copie pour sécurité
    }

    /** Ajoute une unité à la liste des unités possédées */
    public void addUnit(Unit unit) {
        units.add(unit);
    }

    /** Retourne toutes les unités possédées */
    public List<Unit> getUnits() {
        return new ArrayList<>(units); // copie pour sécurité
    }
}
