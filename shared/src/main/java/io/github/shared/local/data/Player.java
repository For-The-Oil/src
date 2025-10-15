package io.github.shared.local.data;

import io.github.shared.local.data.gameobject.Building;
import io.github.shared.local.data.gameobject.Deck;
import io.github.shared.local.data.gameobject.Unit;

import java.util.*;

/**
 * Représente un joueur avec plusieurs Decks, bâtiments et unités.
 */
public class Player {

    private final String name;
    private final Deck deck;
    private final ArrayList<Building> buildings;
    private final ArrayList<Unit> units;

    public Player(String name) {
        this.name = name;
        this.deck = new Deck();
        this.buildings = new ArrayList<>();
        this.units = new ArrayList<>();
    }

    public String getName() {
        return name;
    }
    public void addBuilding(Building building) {
        buildings.add(building);
    }

    public void removeBuilding(Building building) {
        buildings.remove(building);
    }

    public List<Building> getBuildings() {
        return buildings;
    }

    public void addUnit(Unit unit) {
        units.add(unit);
    }

    public void removeUnit(Unit unit) {
        units.remove(unit);
    }

    public List<Unit> getUnits() {
        return units;
    }
}
