package io.github.shared.local.data.order;

import io.github.shared.local.data.Order;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Représente un ordre d'attaque donné à un ensemble d'unités
 * sur une ou plusieurs cibles (unités ou bâtiments).
 * Stocke uniquement les IDs pour être sérialisable et indépendant de l'état local.
 */
public class AttackOrder extends Order implements Serializable {

    private final ArrayList<Integer> unitIds;
    private final int targetUnitId;
    private final int targetBuildingId;

    public AttackOrder(ArrayList<Integer> unitIds, int targetUnitId, int targetBuildingId) {
        super();
        this.unitIds = unitIds;
        this.targetUnitId = targetUnitId;
        this.targetBuildingId = targetBuildingId;
    }

    public ArrayList<Integer> getUnitIds() {
        return unitIds;
    }

    public int getTargetUnitId() {
        return targetUnitId;
    }

    public int getTargetBuildingId() {
        return targetBuildingId;
    }

}
