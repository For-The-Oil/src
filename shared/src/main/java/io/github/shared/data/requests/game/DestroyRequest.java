package io.github.shared.data.requests.game;

import java.util.ArrayList;

import io.github.shared.data.requests.Request;

public class DestroyRequest extends Request {
    private ArrayList<Integer> toKill;

    public DestroyRequest(){
        super();
        this.toKill = new ArrayList<>();
    }


    public ArrayList<Integer> getToKill() {
        return toKill;
    }

    public void setToKill(ArrayList<Integer> toKill) {
        this.toKill = toKill;
    }

    public void add(int netId) {
        this.toKill.add(netId);
    }
}
