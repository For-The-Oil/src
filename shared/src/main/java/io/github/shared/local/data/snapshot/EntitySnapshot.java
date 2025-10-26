package io.github.shared.local.data.snapshot;

import java.io.Serializable;
import java.util.ArrayList;

public class EntitySnapshot implements Serializable {
    public int netId; // identifiant unique
    public ArrayList<ComponentSnapshot> components; // liste des composants sérialisés
}
