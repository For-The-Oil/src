package io.github.shared.local.data.instructions;

import java.util.ArrayList;
import java.util.UUID;

import io.github.shared.local.data.EnumsTypes.EntityType;

public class CreateInstruction extends Instruction{
    private ArrayList<EntityType> toSpawn;
    private ArrayList<Integer> netId;
    private ArrayList<Integer> from;
    private ArrayList<Integer> posX;
    private ArrayList<Integer> posY;
    private UUID player;
    public CreateInstruction(){}
    public CreateInstruction(long timestamp, ArrayList<Integer> netId, ArrayList<Integer> from, ArrayList<Integer> posX, ArrayList<Integer> posY, UUID player) {
        super(timestamp);
        this.netId = netId;
        this.from = from;
        this.posX = posX;
        this.posY = posY;
        this.player = player;
        this.toSpawn = new ArrayList<EntityType>();
    }

    public ArrayList<EntityType> getToSpawn() {
        return toSpawn;
    }

    public UUID getPlayer() {
        return player;
    }

    public ArrayList<Integer> getFrom() {
        return from;
    }

    public ArrayList<Integer> getPosX() {
        return posX;
    }

    public ArrayList<Integer> getPosY() {
        return posY;
    }

    public ArrayList<Integer> getNetId() {
        return netId;
    }
}
