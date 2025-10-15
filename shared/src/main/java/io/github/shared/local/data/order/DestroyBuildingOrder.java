package io.github.shared.local.data.order;

import io.github.shared.local.data.Order;
import io.github.shared.local.data.gameobject.Unit;
import io.github.shared.local.data.gameobject.Building;

import java.io.Serializable;
import java.util.List;

/**
 * Représente un ordre de destruction donné par le joueur.
 * Peut concerner une ou plusieurs unités ou bâtiments.
 */
public class DestroyBuildingOrder extends Order implements Serializable {

    private final List<Building> targetBuildings;

    public DestroyBuildingOrder(List<Building> targetBuildings) {
        super();
        this.targetBuildings = targetBuildings;
    }

    /** Retourne les bâtiments ciblés */
    public List<Building> getTargetBuildings() {
        return targetBuildings;
    }


    /** Vérifie si l'ordre est vide */
    public boolean isEmpty() {
        return targetBuildings.isEmpty();
    }
}
