package io.github.server.game_engine.gdx_ai;

import com.badlogic.gdx.ai.pfa.Connection;

public class WeightedConnection implements Connection<MapNode> {
    private final MapNode fromNode;
    private final MapNode toNode;
    private final float cost;

    public WeightedConnection(MapNode fromNode, MapNode toNode, float cost) {
        this.fromNode = fromNode;
        this.toNode = toNode;
        this.cost = cost;
    }

    @Override
    public float getCost() {
        return cost;
    }

    @Override
    public MapNode getFromNode() {
        return fromNode;
    }

    @Override
    public MapNode getToNode() {
        return toNode;
    }
}
