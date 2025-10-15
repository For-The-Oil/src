package io.github.shared.local.data.order;

import java.io.Serializable;

import io.github.shared.local.data.Order;
import io.github.shared.local.data.gameobject.Building;

/** Construction de bâtiments à une position donnée */
public class ProductionBuildingOrder extends Order implements Serializable {
    private final Building building;
    private final int posX, posY;

    public ProductionBuildingOrder(Building building, int posX, int posY) {
        super();
        this.building = building;
        this.posX = posX;
        this.posY = posY;
    }

    public Building getBuilding() {
        return building;
    }

    public float getPosX() {
        return posX;
    }

    public float getPosY() {
        return posY;
    }

}
