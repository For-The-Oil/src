package io.github.shared.local.data.instructions;

import java.util.HashMap;
import java.util.UUID;

import io.github.shared.local.data.EnumsTypes.RessourcesType;

public class ResourcesInstruction extends Instruction{

    private HashMap<RessourcesType, Integer> ressources;
    private UUID player;
    public ResourcesInstruction(){}
    public ResourcesInstruction(long timestamp, HashMap<RessourcesType, Integer> ressources, UUID player) {
        super(timestamp);
        this.ressources = ressources;
        this.player = player;
    }

    public HashMap<RessourcesType, Integer> getRessources() {
        return ressources;
    }

    public UUID getPlayer() {
        return player;
    }
}
