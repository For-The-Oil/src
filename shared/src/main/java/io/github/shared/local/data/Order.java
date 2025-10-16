package io.github.shared.local.data;

import java.io.Serializable;

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
