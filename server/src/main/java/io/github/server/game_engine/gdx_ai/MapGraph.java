package io.github.server.game_engine.gdx_ai;

import com.artemis.ComponentMapper;
import com.artemis.Entity;
import com.artemis.World;
import com.badlogic.gdx.ai.pfa.Connection;
import com.badlogic.gdx.ai.pfa.indexed.IndexedGraph;
import com.badlogic.gdx.utils.Array;

import java.util.ArrayList;

import io.github.shared.config.BaseGameConfig;
import io.github.shared.data.EnumsTypes.EntityType;
import io.github.shared.data.EnumsTypes.MapName;
import io.github.shared.data.EnumsTypes.WeaponType;
import io.github.shared.data.component.LifeComponent;
import io.github.shared.data.gameobject.Cell;
import io.github.shared.data.gameobject.Shape;

/**
 * Grid-based navigation graph for gdx-ai (A* pathfinding) with dynamic costs.
 *
 * Features:
 *  - Supports entities occupying more than 1x1 cells (unitSize x unitSize).
 *  - Traversability and movement cost depend on the moving EntityType.
 *  - Anti corner-cutting for diagonal moves.
 *  - Optional "break-and-go" behavior: if a cell is breakable, the path can include
 *    the cost to destroy it based on ECS data (World/Entity/LifeComponent).
 *  - Propagates a list of already "destroyed" netIds along the path so you never
 *    pay twice for destroying the same obstacle.
 *
 * Contract: Implements IndexedGraph<MapNode> so gdx-ai can map
 * nodes to indices via getIndex(node) for its internal bookkeeping.
 */
public class MapGraph implements IndexedGraph<MapNode> {

    /** The map grid (width/height + cell access). */
    private final Shape map;

    private final MapName mapName;

    /** Artemis-ODB world; used to fetch entities for breakable cost calculations. */
    private final World world;

    /** Total number of nodes = width * height. */
    private final int nodeCount;

    /**
     * Goal node (unique instance).
     * This instance is used to replace the "to" node when generating connections
     * if the target area overlaps the goal coordinate, ensuring object identity for the goal.
     * That identity can be required by certain pathfinder flows to detect goal reached.
     */
    private final MapNode nodeEnd;

    /** Type of the moving entity (affects traversability and movement costs). */
    private final EntityType entityType;

    /** Entity footprint size (square): 1 => 1x1, 2 => 2x2, etc. */
    private final int unitSize;

    /**
     * @param map        grid (Shape) with width/height and cell access
     * @param world      ECS world; may be null if breakable paths are not used
     * @param nodeEnd    shared goal node instance for the current search
     * @param entityType moving entity type (for terrain rules and weapon stats)
     * @param unitSize   square footprint size (number of cells on a side)
     */
    public MapGraph(Shape map, MapName mapName, World world, MapNode nodeEnd, EntityType entityType, int unitSize) {
        this.map = map;
        this.mapName = mapName;
        this.world = world;
        this.nodeEnd = nodeEnd;
        this.entityType = entityType;
        this.unitSize = unitSize;
        this.nodeCount = map.getWidth() * map.getHeight();
    }

    /**
     * Unique index for a node used by IndexedAStarPathFinder.
     * Convention: index = y * width + x.
     */
    @Override
    public int getIndex(MapNode node) {
        return node.y * map.getWidth() + node.x;
    }

    /** Total node count in this graph. */
    @Override
    public int getNodeCount() {
        return nodeCount;
    }

    /**
     * Returns all outgoing connections from a given node (4-neighbors + diagonals).
     *
     * Steps:
     *  1) Compute candidate neighbor position (nx, ny).
     *  2) Validate destination area for the entity footprint (isValidPosition).
     *  3) For diagonals, prevent corner cutting by requiring both adjacent
     *     orthogonal cells to be traversable.
     *  4) Compute traversal cost (base + per-cell + effects + optional breaking).
     *  5) If cost is finite, create the target MapNode and, if the entity’s
     *     footprint overlaps the goal coordinate, substitute with the shared nodeEnd
     *     (and propagate destroyed-netId trace to nodeEnd).
     */
    @Override
    public Array<Connection<MapNode>> getConnections(MapNode fromNode) {
        Array<Connection<MapNode>> connections = new Array<>();

        // 8 directions: 4 orthogonal + 4 diagonal
        int[][] dirs = {
            { 1,  0}, { -1,  0}, { 0,  1}, { 0, -1}, // orthogonals
            { 1,  1}, { -1, -1}, { 1, -1}, { -1,  1} // diagonals
        };

        for (int[] d : dirs) {
            int nx = fromNode.x + d[0];
            int ny = fromNode.y + d[1];

            // (1) destination must be valid for the entity footprint
            if (!isValidPosition(nx, ny)) continue;

            // (2) anti corner-cutting for diagonals:
            // Both orthogonal neighbors touching the corner must be traversable.
            if (d[0] != 0 && d[1] != 0) { // diagonal move
                Cell cell1 = map.getCells(fromNode.x + d[0], fromNode.y);
                Cell cell2 = map.getCells(fromNode.x,         fromNode.y + d[1]);
                if (cell1 == null || !cell1.getCellType().isTraversable(entityType)) continue;
                if (cell2 == null || !cell2.getCellType().isTraversable(entityType)) continue;
            }

            // (3) base cost: 1 for orthogonal, ~sqrt(2)=1.41 for diagonal
            float baseCost = (d[0] != 0 && d[1] != 0) ? 1.41f : 1f;

            // Clone the "destroyed netIds" trace so each branch has its own list.
            ArrayList<Integer> netIdlst = new ArrayList<>(fromNode.getLstNetId());

            // (4) compute full traversal cost for the destination footprint
            float cost = getTraversalCost(baseCost, nx, ny, netIdlst);
            if (cost < Float.POSITIVE_INFINITY) {
                // Create the target node; may be replaced by nodeEnd if we overlap the goal.
                MapNode newNode = new MapNode(nx, ny, netIdlst);

                // If the entity is bigger than 1x1, check whether any covered cell equals nodeEnd.
                // If yes, use the unique nodeEnd instance (ensures goal identity), and
                // propagate the netId trace into nodeEnd.
                for (int dx = 0; dx < unitSize; dx++) {
                    for (int dy = 0; dy < unitSize; dy++) {
                        if (nx + dx == nodeEnd.x && ny + dy == nodeEnd.y) {
                            newNode = nodeEnd;
                            nodeEnd.addLstNetId(netIdlst);
                        }
                    }
                }

                // Add the weighted connection to the result list
                connections.add(new WeightedConnection(fromNode, newNode, cost));
            }
        }
        return connections;
    }

    /**
     * Validates that the top-left corner (x, y) is inside the map and that the entire
     * entity footprint (x..x+unitSize-1, y..y+unitSize-1) is traversable for the current entityType.
     */
    private boolean isValidPosition(int x, int y) {
        // Bounds check for the entire footprint
        if (x < 0 || y < 0
            || x + unitSize - 1 >= map.getWidth()
            || y + unitSize - 1 >= map.getHeight()) return false;

        // Check traversability of all cells covered by the entity
        for (int dx = 0; dx < unitSize; dx++) {
            for (int dy = 0; dy < unitSize; dy++) {
                Cell cell = map.getCells(x + dx, y + dy);
                if (cell == null || !cell.getCellType().isTraversable(entityType)) return false;
            }
        }
        return true;
    }

    /**
     * Computes the traversal cost for moving the entity footprint to (x, y),
     * including:
     *  - per-cell movement cost (depends on cell type and entityType),
     *  - optional destruction cost if a cell is breakable,
     *  - a multiplicative effect factor (e.g., slows/terrain effects),
     * multiplied by the base cost (1 or ~1.41).
     *
     * Returns +∞ if any covered cell is impassable.
     *
     * @param baseCost  1 (orthogonal) or ~1.41 (diagonal)
     * @param x,y       top-left cell of the destination footprint
     * @param netIdlst  the list of already "destroyed" netIds along this branch
     */
    private float getTraversalCost(float baseCost, int x, int y, ArrayList<Integer> netIdlst) {
        float cost = 0f;
        float costMux = baseCost; // multiplicative factor starts with the base move cost

        for (int dx = 0; dx < unitSize; dx++) {
            for (int dy = 0; dy < unitSize; dy++) {
                Cell cell = map.getCells(x + dx, y + dy);
                if (cell == null) return Float.POSITIVE_INFINITY;

                float tempCost;

                // 1) intrinsic movement cost if walkable for entityType
                float baseWalkable = cell.getCellType().getMovementCost(entityType);
                if (baseWalkable > 0) {
                    tempCost = baseWalkable;
                }
                // 2) otherwise, if the cell is breakable, compute destruction cost
                else if (cell.isBreakable()) {

                    // Look up the cell that would exist *after* destruction (the revealed terrain).
                    // This relies on 'mapName' to provide a shape representing the post-destruction state.
                    Cell breakedCell  = mapName.getShapeType().getShape().getCells(x + dx, y + dy);

                    // If the revealed cell is walkable for this entity, total cost is:
                    //   cost_to_destroy + movement_cost_on_revealed_cell
                    // Otherwise, the move is invalid (infinite).
                    baseWalkable = breakedCell.getCellType().getMovementCost(entityType);
                    if (baseWalkable > 0) {
                        tempCost = buildingDestroyCost(cell, netIdlst);
                        tempCost += baseWalkable;
                    }else {
                        tempCost = Float.POSITIVE_INFINITY;
                    }
                }
                // 3) otherwise, the cell cannot be traversed
                else {
                    tempCost = Float.POSITIVE_INFINITY;
                }

                // If any covered cell is impossible, the whole move is blocked
                if (tempCost == Float.POSITIVE_INFINITY) return tempCost;

                // Accumulate the additive part
                cost += tempCost;

                // Apply per-cell multiplicative effect (e.g., terrain slow)
                costMux *= cell.getEffectType().getMovingCost();
            }
        }
        // Final cost = (sum of cell costs) * (product of effects) * (baseCost)
        return cost * costMux;
    }

    /**
     * Computes the "destroy-to-pass" cost for a breakable cell.
     * - Uses ECS to fetch the entity (by netId) and its LifeComponent (HP/Armor).
     * - Uses weapon stats from the moving entity type (damage and armor penetration).
     * - Scales via BaseGameConfig constants.
     * - Ensures we don’t pay twice for the same obstacle on this path branch by using netIdlst.
     *
     * @param cell     a breakable cell
     * @param netIdlst list of netIds already accounted for in this branch
     * @return the destruction cost, or +∞ if not available/possible
     */
    private float buildingDestroyCost(Cell cell, ArrayList<Integer> netIdlst) {
        int netId = cell.getNetId();

        // If we already "paid" for destroying this netId earlier in this branch, cost is zero
        if (netIdlst.contains(netId)) return 0f;
        netIdlst.add(netId);

        // Fetch the ECS entity to get HP/armor
        final Entity entity = world.getEntity(netId);
        if (entity == null || !entity.isActive()) return Float.POSITIVE_INFINITY;

        final ComponentMapper<LifeComponent> lifeMapper = world.getMapper(LifeComponent.class);
        final LifeComponent lifeComponent = lifeMapper.get(entity);
        if (lifeComponent == null) return Float.POSITIVE_INFINITY;

        // Weapon power of the moving entity (projectile variant if present, else base weapon)
        float damage, armorPenetration;
        if (entityType.getWeaponType().getType().equals(WeaponType.Type.ProjectileLauncher)
            && entityType.getWeaponType().getProjectileType() != null) {
            damage = entityType.getWeaponType().getProjectileType().getDamage();
            armorPenetration = entityType.getWeaponType().getProjectileType().getArmorPenetration();
        } else {
            damage = entityType.getWeaponType().getDamage();
            armorPenetration = entityType.getWeaponType().getArmorPenetration();
        }

        // Cost formula:
        return (float) (
            BaseGameConfig.DESTROY_PATH_COST *
                (lifeComponent.health / (
                    damage * Math.pow(BaseGameConfig.ARMOR_COEF,
                        (armorPenetration >= lifeComponent.armor ? 0 : (lifeComponent.armor - armorPenetration)))))

        );
    }
}
