package io.github.server.game_engine.gdx_ai;
import com.badlogic.gdx.ai.pfa.Heuristic;

public class MapHeuristic implements Heuristic<MapNode> {
    @Override
    public float estimate(MapNode node, MapNode endNode) {
        return (float) Math.sqrt(Math.pow(node.x - endNode.x, 2) + Math.pow(node.y - endNode.y, 2));
    }
}

