package io.github.shared.data.snapshot;

import java.io.Serializable;
import java.util.ArrayList;

import io.github.shared.data.enums_types.EntityType;

public class EntitySnapshot implements Serializable {
    private int netId; // identifiant unique

    private EntityType entityType;
    private ArrayList<ComponentSnapshot> components; // liste des composants sérialisés

    public EntitySnapshot() {}
    public EntitySnapshot(int netId, EntityType entityType, ArrayList<ComponentSnapshot> components) {
        this.netId = netId;
        this.entityType = entityType;
        this.components = components;
    }

    public int getNetId() {
        return netId;
    }

    public void setNetId(int netId) {
        this.netId = netId;
    }

    public EntityType getEntityType() {
        return entityType;
    }

    public void setEntityType(EntityType entityType) {
        this.entityType = entityType;
    }

    public ArrayList<ComponentSnapshot> getComponentSnapshot() {
        return components;
    }

    public void setComponents(ArrayList<ComponentSnapshot> components) {
        this.components = components;
    }
}
