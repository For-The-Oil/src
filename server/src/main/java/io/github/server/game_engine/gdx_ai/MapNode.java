package io.github.server.game_engine.gdx_ai;

import java.util.ArrayList;
import java.util.Objects;

public class MapNode {
    public final int x, y;
    private final ArrayList<Integer> lstNetId;

    public MapNode(int x, int y,ArrayList<Integer> netIdlst) {
        this.x = x;
        this.y = y;
        this.lstNetId = new ArrayList<>();
        this.lstNetId.addAll(netIdlst);

    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        MapNode other = (MapNode) obj;
        return x == other.x && y == other.y;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }


    public ArrayList<Integer> getLstNetId() {
        return lstNetId;
    }

    public void addLstNetId(ArrayList<Integer> lstNetId) {
        this.lstNetId.addAll(lstNetId);
    }
}
