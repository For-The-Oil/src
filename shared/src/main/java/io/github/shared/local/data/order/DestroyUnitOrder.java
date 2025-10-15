package io.github.shared.local.data.order;

import io.github.shared.local.data.Order;
import io.github.shared.local.data.gameobject.Unit;

import java.io.Serializable;
import java.util.List;

/**
 * Représente un ordre donné par le joueur pour tuer ou attaquer des unités ennemies.
 */
public class DestroyUnitOrder extends Order implements Serializable {

    private final List<Unit> targetUnits;

    public DestroyUnitOrder(List<Unit> targetUnits) {
        super();
        this.targetUnits = targetUnits;
    }

    /** Retourne la liste des unités ciblées */
    public List<Unit> getTargetUnits() {
        return targetUnits;
    }

    /** Vérifie si l'ordre est vide */
    public boolean isEmpty() {
        return targetUnits.isEmpty();
    }
}
