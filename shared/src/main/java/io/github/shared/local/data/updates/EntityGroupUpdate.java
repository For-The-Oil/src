package io.github.shared.local.data.updates;

import java.awt.Component;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

import io.github.shared.local.data.snapshot.EntitySnapshot;

public class EntityGroupUpdate extends Update{

    //Composants Sérializer associé à une NetID
    private ArrayList<EntitySnapshot> toSpawn;
    private ArrayList<EntitySnapshot> toUpdate;

    //NetID indiquant quels entités à détruire
    private ArrayList<Integer> toKill;

    public EntityGroupUpdate(){}
    public EntityGroupUpdate(long timestamp) {
        super(timestamp);
        this.toSpawn = new ArrayList<EntitySnapshot>();
        this.toUpdate = new ArrayList<EntitySnapshot>();

        this.toKill = new ArrayList<Integer>();
    }
}
