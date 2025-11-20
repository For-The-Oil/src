package io.github.shared.data.requests.game;

import java.util.ArrayList;

import io.github.shared.data.requests.Request;

public class AttackGroupRequest extends Request {
    private ArrayList<Integer> group;
    private int targetNetId;

    public AttackGroupRequest(){super();}


    public ArrayList<Integer> getGroup() {
        return group;
    }

    public void setGroup(ArrayList<Integer> group) {
        this.group = group;
    }

    public int getTargetNetId() {
        return targetNetId;
    }

    public void setTargetNetId(int targetNetId) {
        this.targetNetId = targetNetId;
    }
}
