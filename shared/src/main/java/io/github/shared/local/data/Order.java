package io.github.shared.local.data;

import java.io.Serializable;

import io.github.shared.local.data.nameEntity.OrderType;

/**
 * Représente un ordre donné par un joueur, avec des paramètres obligatoires selon le type.
 */
public class Order implements Serializable {
    private final long timestamp;
    private final OrderType order;

    public Order(OrderType order) {
        this.timestamp = System.currentTimeMillis();
        this.order = order;
    }

    public long getTimestamp() {
        return timestamp;
    }

}
