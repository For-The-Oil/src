package io.github.shared.local.data.order;

import io.github.shared.local.data.Order;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Représente un ordre d'attaque donné à un ensemble d'unités
 * sur une ou plusieurs cibles (unités ou bâtiments).
 * Stocke uniquement les IDs pour être sérialisable et indépendant de l'état local.
 */
public class AttackOrder extends Order implements Serializable {

    private final List<Integer> unitIds;          // unités qui attaquent
    private final int targetUnitId;    // unités cibles
    private final int targetBuildingId;// bâtiments cibles

    public AttackOrder(List<Integer> unitIds, int targetUnitId, int targetBuildingId) {
        super();
        this.unitIds = new ArrayList<>(unitIds);
        this.targetUnitId = targetUnitId;
        this.targetBuildingId = targetBuildingId;
    }

    /** Unmodifiable view des unités qui attaquent */
    public List<Integer> getUnitIds() {
        return Collections.unmodifiableList(unitIds);
    }

    /** Unmodifiable view de l'unité cible */
    public int getTargetUnitId() {
        return targetUnitId;
    }

    /** Unmodifiable view du bâtiment cible */
    public int getTargetBuildingId() {
        return targetBuildingId;
    }

}
