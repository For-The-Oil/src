package io.github.shared.local.data.order;

import java.io.Serializable;
import java.util.ArrayList;

import io.github.shared.local.data.Order;
import io.github.shared.local.data.gameobject.Unit;

/** Déplacement d'unités vers une position */
public class MoveUnitOrder extends Order implements Serializable {
    private final ArrayList<Unit> units;
    private final float targetX, targetY;

    public MoveUnitOrder(ArrayList<Unit> units, float targetX, float targetY) {
        super();
        this.units = new ArrayList<>(units);
        this.targetX = targetX;
        this.targetY = targetY;
    }

    public ArrayList<Unit> getUnits() {
        return units;
    }

    public float getTargetX() {
        return targetX;
    }

    public float getTargetY() {
        return targetY;
    }

}
