package io.github.shared.data.requests.game;

import java.util.ArrayList;

import io.github.shared.data.requests.Request;

public class MoveGroupRequest extends Request {
    private ArrayList<Integer> group;
    private boolean targetRelated;
    private boolean force;
    private float posX;
    private float posY;

    public void reset() {
        posX = -1;
        posY = -1;
        targetRelated = false;
        force = false;
    }

    public MoveGroupRequest(){super();}
    public MoveGroupRequest(ArrayList<Integer> group, float posX, float posY,boolean force){super();
        this.targetRelated = false;
        this.force = force;
        this.group = group;
        this.posX = posX;
        this.posY = posY;
    }

    public MoveGroupRequest(ArrayList<Integer> group, boolean targetRelated,boolean force){super();
        this.targetRelated = targetRelated;
        this.force = force;
        this.group = group;
        this.posX = -1;
        this.posY = -1;
    }

    public ArrayList<Integer> getGroup() {
        return group;
    }

    public void setGroup(ArrayList<Integer> group) {
        this.group = group;
    }

    public float getPosX() {
        return posX;
    }

    public void setPosX(float posX) {
        this.posX = posX;
    }

    public float getPosY() {
        return posY;
    }

    public void setPosY(float posY) {
        this.posY = posY;
    }

    public boolean isTargetRelated() {
        return targetRelated;
    }

    public void setTargetRelated(boolean targetRelated) {
        this.targetRelated = targetRelated;
    }

    public boolean isForce() {
        return force;
    }

    public void setForce(boolean force) {
        this.force = force;
    }
}
