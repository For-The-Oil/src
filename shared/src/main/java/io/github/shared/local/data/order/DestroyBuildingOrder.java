package io.github.shared.local.data.order;

import io.github.shared.local.data.Order;
import io.github.shared.local.data.gameobject.Building;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Représente un ordre de destruction donné par le joueur.
 * Peut concerner une ou plusieurs unités ou bâtiments.
 */
public class DestroyBuildingOrder extends Order implements Serializable {

    private final ArrayList<Building> targetBuildings;

    public DestroyBuildingOrder(ArrayList<Building> targetBuildings) {
        super();
        this.targetBuildings = targetBuildings;
    }
    public ArrayList<Building> getTargetBuildings() {
        return targetBuildings;
    }
}
