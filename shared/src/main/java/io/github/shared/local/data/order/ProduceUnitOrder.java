package io.github.shared.local.data.order;

import java.io.Serializable;

import io.github.shared.local.data.Order;
import io.github.shared.local.data.gameobject.Building;

/** Production d'unités depuis un bâtiment */
public class ProduceUnitOrder extends Order implements Serializable {
    private final Building building;
    private final int quantity;
    private String name;//On ne va pas utiliser le nom mais Sûrement autre chose permettant de reconnaître Un type unit comme Soldat par exemple

    public ProduceUnitOrder(Building building, int quantity, String name) {
        super();
        this.building = building;
        this.quantity = quantity;
        this.name = name;
    }

    public Building getBuilding() {
        return building;
    }

    public int getQuantity() {
        return quantity;
    }
}
