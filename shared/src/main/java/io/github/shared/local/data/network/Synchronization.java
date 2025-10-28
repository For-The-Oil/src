package io.github.shared.local.data.network;

import java.util.ArrayList;
import java.util.HashMap;

import io.github.shared.local.data.EnumsTypes.EventType;
import io.github.shared.local.data.gameobject.Shape;
import io.github.shared.local.data.snapshot.EntitySnapshot;

class Synchronization {
    private ArrayList<EntitySnapshot> entities;
    private ArrayList<Player> players;
    private HashMap<String, String> teams; //Team by UUID
    private Shape map;
    private EventType currentEvent;
    private long time_left;  //seconds
    private long timestamp; // utile pour ordre des updates

    public Synchronization(){ }
}
