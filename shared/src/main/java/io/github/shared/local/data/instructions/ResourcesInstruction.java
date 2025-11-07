package io.github.shared.local.data.instructions;

import java.util.HashMap;

import io.github.shared.local.data.EnumsTypes.RessourcesType;

public class ResourcesInstruction extends Instruction{

    private HashMap<RessourcesType, Integer> ressources;
    public String player;
    public ResourcesInstruction(){}
    public ResourcesInstruction(long timestamp, HashMap<RessourcesType, Integer> ressources) {
        super(timestamp);
        this.ressources = ressources;
    }
}
