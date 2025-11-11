package io.github.server.gdx_ai;

import com.artemis.ComponentMapper;
import com.artemis.Entity;
import com.artemis.World;
import com.badlogic.gdx.ai.pfa.Connection;
import com.badlogic.gdx.ai.pfa.indexed.IndexedGraph;
import com.badlogic.gdx.utils.Array;

import java.util.ArrayList;

import io.github.server.config.BaseGameConfig;
import io.github.shared.local.data.EnumsTypes.EntityType;
import io.github.shared.local.data.EnumsTypes.WeaponType;
import io.github.shared.local.data.component.LifeComponent;
import io.github.shared.local.data.gameobject.Cell;
import io.github.shared.local.data.gameobject.Shape;

public class MapGraph implements IndexedGraph<MapNode> {
    private final Shape map;
    private final World world;
    private final int nodeCount;
    private final EntityType entityType;
    private final int unitSize;

    public MapGraph(Shape map, World world, EntityType entityType, int unitSize) {
        this.map = map;
        this.world = world;
        this.entityType = entityType;
        this.unitSize = unitSize;
        this.nodeCount = map.getWidth() * map.getHeight();
    }

    @Override
    public int getIndex(MapNode node) {
        return node.y * map.getWidth() + node.x;
    }

    @Override
    public int getNodeCount() {
        return nodeCount;
    }

    @Override
    public Array<Connection<MapNode>> getConnections(MapNode fromNode) {
        Array<Connection<MapNode>> connections = new Array<>();
        int[][] dirs = {
            {1, 0}, {-1, 0}, {0, 1}, {0, -1}, // orthogonales
            {1, 1}, {-1, -1}, {1, -1}, {-1, 1} // diagonales
        };

        for (int[] d : dirs) {
            int nx = fromNode.x + d[0];
            int ny = fromNode.y + d[1];

            // Vérifie si la position est valide pour l'unité
            if (!isValidPosition(nx, ny)) continue;

            // Logique anti-traversée de coin pour diagonales
            if (d[0] != 0 && d[1] != 0) { // diagonale
                Cell cell1 = map.getCells(fromNode.x + d[0], fromNode.y);
                Cell cell2 = map.getCells(fromNode.x, fromNode.y + d[1]);
                if (cell1 == null || cell1.getCellType().isTraversable(entityType)) continue;
                if (cell2 == null || cell2.getCellType().isTraversable(entityType)) continue;
            }

            // Calcul du coût avec diagonale (√2)
            float baseCost = (d[0] != 0 && d[1] != 0) ? 1.41f : 1f;
            ArrayList<Integer> netIdlst = new ArrayList<>(fromNode.getLstNetId());
            float cost = getTraversalCost(baseCost,nx, ny,netIdlst);
            if (cost < Float.POSITIVE_INFINITY) {
                connections.add(new WeightedConnection(fromNode, new MapNode(nx, ny, netIdlst), cost));
            }
        }
        return connections;
    }


    private boolean isValidPosition(int x, int y) {
        if (x < 0 || y < 0 || x + unitSize > map.getWidth() || y + unitSize > map.getHeight()) return false;
        for (int dx = 0; dx < unitSize; dx++) {
            for (int dy = 0; dy < unitSize; dy++) {
                Cell cell = map.getCells(x + dx, y + dy);
                if (cell == null || cell.getCellType().isTraversable(entityType)) return false;
            }
        }
        return true;
    }

    private float getTraversalCost(float baseCost, int x, int y, ArrayList<Integer> netIdlst) {
        float cost = 0f;
        float costMux = baseCost;
        for (int dx = 0; dx < unitSize; dx++) {
            for (int dy = 0; dy < unitSize; dy++) {
                Cell cell = map.getCells(x + dx, y + dy);
                if (cell == null) return Float.POSITIVE_INFINITY;
                float tempCost;
                // Capacité de l'entité (walkable, flyable, etc.)
                float baseWalkable = cell.getCellType().getMovementCost(entityType);
                if (baseWalkable > 0) tempCost = baseWalkable;
                else if(cell.isBreakable()) tempCost = buildingDestroyCost(cell,netIdlst);
                else tempCost = Float.POSITIVE_INFINITY;
                if(tempCost == Float.POSITIVE_INFINITY)return tempCost;
                cost +=tempCost;

                // Effets de la cellule
                costMux *= cell.getEffectType().getMovingCost();
            }
        }
        return cost*costMux;
    }
    private float buildingDestroyCost(Cell cell, ArrayList<Integer> netIdlst) {
        int netId = cell.getNetId();
        if(netIdlst.contains(netId))return 0f;
        netIdlst.add(netId);
        final Entity entity = world.getEntity(netId);
        if (entity == null || !entity.isActive()) return Float.POSITIVE_INFINITY;
        final ComponentMapper<LifeComponent> lifeMapper = world.getMapper(LifeComponent.class);
        final LifeComponent lifeComponent = lifeMapper.get(entity);
        if (lifeComponent == null ) return Float.POSITIVE_INFINITY;
        float damage,armorPenetration;
        if(entityType.getWeaponType().getType().equals(WeaponType.Type.ProjectileLauncher)&&entityType.getWeaponType().getProjectileType()!=null){
            damage = entityType.getWeaponType().getProjectileType().getDamage();
            armorPenetration = entityType.getWeaponType().getProjectileType().getArmorPenetration();
        }
        damage = entityType.getWeaponType().getDamage();
        armorPenetration = entityType.getWeaponType().getArmorPenetration();
        return (float)(BaseGameConfig.DESTROY_PATH_COST * (lifeComponent.health / ( damage * Math.max(1, Math.pow( BaseGameConfig.ARMOR_COEF,(armorPenetration-lifeComponent.armor) )))));
    }

}
