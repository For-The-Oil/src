package io.github.shared.local.data.instructions;

import java.util.HashMap;

import io.github.shared.local.data.EnumsTypes.RessourcesType;

public class RessourcesInstruction extends Instruction{

    private HashMap<RessourcesType, Integer> ressources;
    public RessourcesInstruction(){}
    public RessourcesInstruction(long timestamp, HashMap<RessourcesType, Integer> ressources) {
        super(timestamp);
        this.ressources = ressources;
    }
}
