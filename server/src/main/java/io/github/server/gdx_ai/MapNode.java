package io.github.server.gdx_ai;

import java.util.ArrayList;

public class MapNode {
    public final int x, y;
    private final ArrayList<Integer> lstNetId;

    public MapNode(int x, int y,ArrayList<Integer> netIdlst) {
        this.x = x;
        this.y = y;
        this.lstNetId = new ArrayList<>();
        this.lstNetId.addAll(netIdlst);

    }

    public ArrayList<Integer> getLstNetId() {
        return lstNetId;
    }
}
