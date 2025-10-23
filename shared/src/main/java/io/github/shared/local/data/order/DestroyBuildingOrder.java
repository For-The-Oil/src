package io.github.shared.local.data.order;

import com.artemis.Entity;

import io.github.shared.local.data.Order;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Représente un ordre de destruction donné par le joueur.
 * Peut concerner une ou plusieurs unités ou bâtiments.
 */
public class DestroyBuildingOrder extends Order implements Serializable {

    private final ArrayList<Entity> targetBuildings;

    public DestroyBuildingOrder(ArrayList<Entity> targetBuildings) {
        super();
        this.targetBuildings = targetBuildings;
    }
    public ArrayList<Entity> getTargetBuildings() {
        return targetBuildings;
    }
}
