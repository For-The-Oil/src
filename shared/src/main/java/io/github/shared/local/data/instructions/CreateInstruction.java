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
    private ArrayList<UUID> player;
    public CreateInstruction(){}
    public CreateInstruction(long timestamp) {
        super(timestamp);
        this.netId = new ArrayList<>();
        this.from = new ArrayList<>();
        this.posX = new ArrayList<>();
        this.posY = new ArrayList<>();
        this.player = new ArrayList<>();
        this.toSpawn = new ArrayList<>();
    }

    public void add(EntityType type, int netId, int from, int posX, int posY, UUID player) {
        this.toSpawn.add(type);
        this.netId.add(netId);
        this.from.add(from);
        this.posX.add(posX);
        this.posY.add(posY);
        this.player.add(player);
    }

    public ArrayList<EntityType> getToSpawn() {
        return toSpawn;
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

    public ArrayList<UUID> getPlayer() {
        return player;
    }

    public void setToSpawn(ArrayList<EntityType> toSpawn) {
        this.toSpawn = toSpawn;
    }

    public void setNetId(ArrayList<Integer> netId) {
        this.netId = netId;
    }

    public void setFrom(ArrayList<Integer> from) {
        this.from = from;
    }

    public void setPosX(ArrayList<Integer> posX) {
        this.posX = posX;
    }

    public void setPosY(ArrayList<Integer> posY) {
        this.posY = posY;
    }

    public void setPlayer(ArrayList<UUID> player) {
        this.player = player;
    }
}
