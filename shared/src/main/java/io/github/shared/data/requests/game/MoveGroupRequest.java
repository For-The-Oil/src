package io.github.shared.data.requests.game;

import java.util.ArrayList;

import io.github.shared.data.requests.Request;

public class MoveGroupRequest extends Request {
    private ArrayList<Integer> group;
    private float posX;
    private float posY;

    public MoveGroupRequest(){super();}
    public MoveGroupRequest(ArrayList<Integer> group, float posX, float posY){super();
        this.group = group;
        this.posX = posX;
        this.posY = posY;
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
}
