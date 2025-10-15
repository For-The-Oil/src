package io.github.shared.local.data.order;

import io.github.shared.local.data.Order;
import io.github.shared.local.data.gameobject.Unit;

import java.io.Serializable;
import java.util.ArrayList;


/**
 * Représente un ordre donné par le joueur pour tuer ou attaquer des unités ennemies.
 */
public class DestroyUnitOrder extends Order implements Serializable {

    private final ArrayList<Unit> targetUnits;

    public DestroyUnitOrder(ArrayList<Unit> targetUnits) {
        super();
        this.targetUnits = targetUnits;
    }
    public ArrayList<Unit> getTargetUnits() {
        return targetUnits;
    }
}
