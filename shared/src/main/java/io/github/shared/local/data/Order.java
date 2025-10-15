package io.github.shared.local.data;

import io.github.shared.local.data.gameobject.Building;
import io.github.shared.local.data.gameobject.Unit;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Représente un ordre donné par un joueur, avec des paramètres obligatoires selon le type.
 */
public abstract class Order implements Serializable {
    private final long timestamp;

    public Order() {
        this.timestamp = System.currentTimeMillis();
    }

    public long getTimestamp() {
        return timestamp;
    }

}
