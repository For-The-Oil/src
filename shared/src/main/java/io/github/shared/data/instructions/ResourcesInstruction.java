package io.github.shared.data.instructions;

import java.util.HashMap;
import java.util.UUID;

import io.github.shared.data.EnumsTypes.ResourcesType;

public class ResourcesInstruction extends Instruction{

    private HashMap<ResourcesType, Integer> ressources;
    private UUID player;
    public ResourcesInstruction(){}
    public ResourcesInstruction(long timestamp, HashMap<ResourcesType, Integer> ressources, UUID player) {
        super(timestamp);
        this.ressources = ressources;
        this.player = player;
    }

    public HashMap<ResourcesType, Integer> getRessources() {
        return ressources;
    }

    public void setRessources(HashMap<ResourcesType, Integer> ressources) {
        this.ressources = ressources;
    }

    public UUID getPlayer() {
        return player;
    }

    public void setPlayer(UUID player) {
        this.player = player;
    }
}
