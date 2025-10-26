package io.github.shared.local.data.updates;

import java.util.HashMap;

import io.github.shared.local.data.EnumsTypes.RessourcesType;

public class RessourcesUpdate extends Update{

    private HashMap<RessourcesType, Integer> ressources;

    public RessourcesUpdate(){}
    public RessourcesUpdate(long timestamp, HashMap<RessourcesType, Integer> ressources) {
        super(timestamp);
        this.ressources = ressources;
    }
}
