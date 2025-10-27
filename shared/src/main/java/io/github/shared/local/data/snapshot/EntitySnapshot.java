package io.github.shared.local.data.snapshot;

import java.io.Serializable;
import java.util.ArrayList;

import io.github.shared.local.data.EnumsTypes.EntityType;

public class EntitySnapshot implements Serializable {
    public int netId; // identifiant unique

    public EntityType entityType;
    public ArrayList<ComponentSnapshot> components; // liste des composants sérialisés
}
