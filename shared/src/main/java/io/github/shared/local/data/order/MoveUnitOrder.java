package io.github.shared.local.data.order;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.github.shared.local.data.Order;
import io.github.shared.local.data.gameobject.Unit;

/** Déplacement d'unités vers une position */
public class MoveUnitOrder extends Order implements Serializable {
    private final List<Unit> units;
    private final float targetX, targetY;

    public MoveUnitOrder(List<Unit> units, float targetX, float targetY) {
        super();
        this.units = new ArrayList<>(units);
        this.targetX = targetX;
        this.targetY = targetY;
    }

    public List<Unit> getUnits() {
        return Collections.unmodifiableList(units);
    }

    public float getTargetX() {
        return targetX;
    }

    public float getTargetY() {
        return targetY;
    }

}
