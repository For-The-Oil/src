
package io.github.server.game_engine.system;

import com.artemis.Aspect;
import com.artemis.Component;
import com.artemis.ComponentMapper;
import com.artemis.annotations.Wire;
import com.artemis.systems.IteratingSystem;
import com.badlogic.gdx.ai.pfa.DefaultGraphPath;
import com.badlogic.gdx.ai.pfa.GraphPath;
import com.badlogic.gdx.ai.pfa.indexed.IndexedAStarPathFinder;
import java.util.HashMap;
import io.github.server.data.ServerGame;
import io.github.server.game_engine.gdx_ai.MapGraph;
import io.github.server.game_engine.gdx_ai.MapHeuristic;
import io.github.server.game_engine.gdx_ai.MapNode;
import io.github.shared.data.enums_types.EntityType;
import io.github.shared.data.enums_types.WeaponType;
import io.github.shared.data.component.*;
import io.github.shared.data.gameobject.Cell;
import io.github.shared.data.snapshot.ComponentSnapshot;
import io.github.shared.shared_engine.Utility;

/**
 * MovementServerSystem
 *
 * Server-side movement system responsible for:
 * - Handling entity movement toward a target or destination.
 * - Applying pathfinding (A*) to compute next waypoint.
 * - Stopping movement when the entity reaches its destination or can attack its target.
 * - Gradually reducing velocity using damping based on delta time.
 * - Sending all updates via snapshots (ComponentSnapshot) to synchronize with clients.
 *
 * Key Features:
 * - Frame-rate independent movement using delta time.
 * - Attack range checks for Melee, Ranged (with LoS), and Projectile attacks.
 * - Terrain-based speed adjustment using movement cost from map cells.
 */
@Wire
public class MovementServerSystem extends IteratingSystem {

    /** Tolerance for considering an entity as having reached its destination. */
    private static final float EPSILON = 0.25f;

    private final ServerGame server;
    private final MapHeuristic heuristic = new MapHeuristic();

    // Component mappers for quick access
    private ComponentMapper<PositionComponent> mPos;
    private ComponentMapper<VelocityComponent> mVel;
    private ComponentMapper<SpeedComponent> mSpeed;
    private ComponentMapper<MoveComponent> mMove;
    private ComponentMapper<TargetComponent> mTarget;
    private ComponentMapper<NetComponent> mNet;
    private ComponentMapper<MeleeAttackComponent> mMelee;
    private ComponentMapper<RangedAttackComponent> mRanged;
    private ComponentMapper<ProjectileAttackComponent> mProjectile;

    /**
     * Constructor: Defines the aspect of entities processed by this system.
     * Entities must have Position, Velocity, Speed, and Net components.
     * Excludes frozen entities, buildings, and projectiles.
     */
    public MovementServerSystem(ServerGame server) {
        super(Aspect.all(PositionComponent.class, SpeedComponent.class, NetComponent.class).exclude(FreezeComponent.class, BuildingMapPositionComponent.class, ProjectileComponent.class));
        this.server = server;
    }

    /**
     * Main processing logic for each entity:
     * - If no movement intent, apply damping to velocity and return.
     * - If target-related but target is invalid, stop movement.
     * - If destination reached, stop movement.
     * - Otherwise, compute path and update velocity toward next waypoint.
     *
     * @param e Entity ID
     */
    @Override
    protected void process(int e) {
        PositionComponent pos = mPos.get(e);
        VelocityComponent vel = mVel.get(e);
        SpeedComponent speed = mSpeed.get(e);
        MoveComponent move = mMove.get(e);
        NetComponent net = mNet.get(e);
        TargetComponent tgt = mTarget.get(e);

        if (pos == null) return;

        // Apply damping to gradually reduce velocity toward zero
        if (vel != null && !vel.isStop())zeroVelocity(vel, pos, e);


        // No movement intent -> nothing else to do
        if (move == null) return;

        // Target-related movement but target is missing -> stop
        if (move.targetRelated && (tgt == null || !tgt.hasTarget())) {
            return;
        }

        float destX, destY;

        if (move.targetRelated) {
            // Get target position using network ID
            PositionComponent tPos = Utility.getPositionByNetId(world, tgt.targetId, mNet, mPos);
            if (tPos == null) return;

            destX = tPos.x;
            destY = tPos.y;

            // Stop if entity can attack the target (within range and LoS)
            if (canAttack(e, pos, tPos)) {
                return;
            }
        } else {
            // Destination-based movement
            destX = move.destinationX;
            destY = move.destinationY;
            if (destX < 0 || destY < 0) return;

            // Stop if destination reached
            if (reached(pos.x, pos.y, destX, destY, EPSILON)) {
                return;
            }
        }
        if(canAttack(e, pos, Utility.getPositionByNetId(world, tgt.nextTargetId, mNet, mPos))){
            return;
        }

        GraphPath<MapNode> path = computePath(pos.x, pos.y, destX, destY, net.entityType);
        if (path.getCount() < 2) return;

        MapNode temp = path.get(path.getCount() - 1);
        if (!temp.getLstNetId().isEmpty()){
            int firstElementTarget = temp.getLstNetId().get(0);

        // Send nextTargetId update via snapshot
        HashMap<String, Object> fieldsTarget = new HashMap<>();
        fieldsTarget.put("targetId", tgt.targetId);
        fieldsTarget.put("nextTargetId", firstElementTarget);
        fieldsTarget.put("force", tgt.force);
        ComponentSnapshot snapTarget = new ComponentSnapshot("TargetComponent", fieldsTarget);
        server.getUpdateTracker().markComponentModified(world.getEntity(e), snapTarget);
        }

        // Get next waypoint
        MapNode next = path.get(1);
        float wx = Utility.cellToWorld(next.x);
        float wy = Utility.cellToWorld(next.y);

        float dx = wx - pos.x;
        float dy = wy - pos.y;
        float len = (float) Math.sqrt(dx * dx + dy * dy);
        if (len == 0f) return;

        // Normalize direction
        dx /= len;
        dy /= len;

        // Compute speed adjusted by terrain
        float terrainMul = terrainSpeedMultiplier(next.x, next.y, net.entityType);
        float base = (speed != null ? speed.getSpeed() : 0f);
        float finalSpeed = base * terrainMul;

        // Apply delta for frame-rate independence
        float delta = world.getDelta();

        // Send velocity update via snapshot
        HashMap<String, Object> fields = new HashMap<>();
        fields.put("vx", dx * finalSpeed * delta);
        fields.put("vy", dy * finalSpeed * delta);
        fields.put("vz", 0f);
        ComponentSnapshot snap = new ComponentSnapshot("VelocityComponent", fields);
        server.getUpdateTracker().markComponentModified(world.getEntity(e), snap);


        MeleeAttackComponent melee = mMelee.get(e);
        ProjectileAttackComponent attack = mProjectile.get(e);
        RangedAttackComponent ranged = mRanged.get(e);
        if(melee!=null)AttackMovingPenalty(melee.weaponType,melee,e);
        if(attack!=null)AttackMovingPenalty(attack.weaponType,attack,e);
        if(ranged!=null)AttackMovingPenalty(ranged.weaponType,ranged,e);
    }

    /**
     * Checks if the entity can attack its target based on attack type.
     * - Melee: distance <= reach
     * - Ranged: distance <= range AND LoS check
     * - Projectile: distance <= range
     */
    private boolean canAttack(int e, PositionComponent attackerPos, PositionComponent targetPos) {
        if(targetPos == null)return false;
        if (mMelee != null && mMelee.has(e)) {
            MeleeAttackComponent melee = mMelee.get(e);
            float reach = melee.reach;
            float dx = targetPos.x - attackerPos.x;
            float dy = targetPos.y - attackerPos.y;
            float dz = targetPos.z - attackerPos.z;
            return (dx * dx + dy * dy + dz * dz) <= (reach * reach);
        }
        if (mRanged != null && mRanged.has(e)) {
            RangedAttackComponent ranged = mRanged.get(e);
            float range = ranged.range;
            return Utility.isRangedDistanceValid(attackerPos, targetPos, range, server.getMap());
        }
        if (mProjectile != null && mProjectile.has(e)) {
            ProjectileAttackComponent proj = mProjectile.get(e);
            float range = proj.range;
            return inRange(attackerPos, targetPos, range);
        }
        return false;
    }

    /** Simple distance check for projectile attacks. */
    private boolean inRange(PositionComponent a, PositionComponent b, float r) {
        float dx = b.x - a.x, dy = b.y - a.y, dz = b.z - a.z;
        return (dx * dx + dy * dy + dz * dz) <= (r * r);
    }

    /**
     * Gradually reduces velocity toward zero using damping based on delta time.
     * This creates a smooth stop instead of an abrupt halt.
     */
    private void zeroVelocity(VelocityComponent vel,PositionComponent pos, int e) {
        float vx = vel.vx;
        float vy = vel.vy;
        float vz = vel.vz;

        float delta = world.getDelta();
        float dampingRate = 5f; // Higher = faster stop

        // Apply damping
        vx -= vx * dampingRate * delta;
        vy -= vy * dampingRate * delta;
        vz -= vz * dampingRate * delta;

        // Snap to zero if speed is very low
        float speed = (float) Math.sqrt(vx * vx + vy * vy + vz * vz);
        if (speed < 0.05f) {
            vx = vy = vz = 0f;
            HashMap<String, Object> fields = new HashMap<>();
            ComponentSnapshot previousSnapshot2 = server.getUpdateTracker().getPreviousSnapshot(world.getEntity(e),"PositionComponent");
            if(previousSnapshot2 == null) {
                fields.put("x", pos.x);
                fields.put("y", pos.y);
                fields.put("z", pos.z);
                fields.put("horizontalRotation", pos.horizontalRotation);
                fields.put("verticalRotation", pos.verticalRotation);
                ComponentSnapshot snap = new ComponentSnapshot("PositionComponent", fields);
                server.getUpdateTracker().markComponentModified(world.getEntity(e), snap);
            }
        }

        // Send snapshot update
        HashMap<String, Object> fields = new HashMap<>();
        fields.put("vx", vx);
        fields.put("vy", vy);
        fields.put("vz", vz);
        ComponentSnapshot snap = new ComponentSnapshot("VelocityComponent", fields);
        server.getUpdateTracker().markComponentModified(world.getEntity(e), snap);
    }

    /** Checks if the entity has reached its destination within a tolerance. */
    private boolean reached(float x, float y, float tx, float ty, float eps) {
        float dx = tx - x, dy = ty - y;
        return (dx * dx + dy * dy) <= eps * eps;
    }

    /**
     * Computes path using A* algorithm.
     * Converts world coordinates to grid cells and validates positions.
     */
    private GraphPath<MapNode> computePath(float sx, float sy, float dx, float dy, EntityType type) {
        int sxg = Utility.worldToCell(sx);
        int syg = Utility.worldToCell(sy);
        int dxg = Utility.worldToCell(dx);
        int dyg = Utility.worldToCell(dy);

        if (!server.getMap().isValidPosition(sxg, syg) || !server.getMap().isValidPosition(dxg, dyg)) {
            return new DefaultGraphPath<>();
        }

        MapNode start = new MapNode(sxg, syg, new java.util.ArrayList<>());
        MapNode end = new MapNode(dxg, dyg, new java.util.ArrayList<>());
        MapGraph graph = new MapGraph(server.getMap(), null, null, end, type, 1);
        IndexedAStarPathFinder<MapNode> finder = new IndexedAStarPathFinder<>(graph);
        GraphPath<MapNode> path = new DefaultGraphPath<>();
        finder.searchNodePath(start, end, heuristic, path);
        return path;
    }

    /** Returns terrain speed multiplier based on cell type and effects. */
    private float terrainSpeedMultiplier(int gx, int gy, EntityType type) {
        if (!server.getMap().isValidPosition(gx, gy)) return 0f;
        Cell cell = server.getMap().getCells(gx, gy);
        if (cell == null || cell.getCellType() == null) return 0f;
        return cell.getCellType().getMovementCost(type) * cell.getEffectType().getMovingCost();
    }

    private void AttackMovingPenalty(WeaponType weaponType,Component component, int e){
        if(!weaponType.isHitAndMove()){
            if (weaponType.getType().equals(WeaponType.Type.Melee)) {
                MeleeAttackComponent meleeAttackComponent = (MeleeAttackComponent) component;
                ComponentSnapshot previousSnapshot = server.getUpdateTracker().getPreviousSnapshot(world.getEntity(e),"MeleeAttackComponent");
                if (previousSnapshot != null) {
                    previousSnapshot.getFields().put("currentCooldown", weaponType.getAnimationAndFocusCooldown());
                } else if(weaponType.getAnimationAndFocusCooldown() > meleeAttackComponent.cooldown) {
                    HashMap<String, Object> fields = new HashMap<>();
                    fields.put("weaponType", weaponType);
                    fields.put("damage", meleeAttackComponent.damage);
                    fields.put("cooldown", weaponType.getCooldown());
                    fields.put("currentCooldown", weaponType.getAnimationAndFocusCooldown());
                    fields.put("reach", meleeAttackComponent.reach);
                    fields.put("horizontalRotation", meleeAttackComponent.horizontalRotation);
                    fields.put("verticalRotation", meleeAttackComponent.verticalRotation);
                    ComponentSnapshot snap = new ComponentSnapshot("MeleeAttackComponent", fields);
                    server.getUpdateTracker().markComponentModified(world.getEntity(e), snap);
                }else meleeAttackComponent.cooldown = weaponType.getAnimationAndFocusCooldown();
            }
            if (weaponType.getType().equals(WeaponType.Type.Range)) {
                RangedAttackComponent rangedAttackComponent = (RangedAttackComponent) component;
                ComponentSnapshot previousSnapshot = server.getUpdateTracker().getPreviousSnapshot(world.getEntity(e), "RangedAttackComponent");
                if (previousSnapshot != null) {
                    previousSnapshot.getFields().put("currentCooldown", weaponType.getAnimationAndFocusCooldown());
                } else if(weaponType.getAnimationAndFocusCooldown() > rangedAttackComponent.cooldown) {
                    HashMap<String, Object> fields = new HashMap<>();
                    fields.put("weaponType", weaponType);
                    fields.put("damage", rangedAttackComponent.damage);
                    fields.put("cooldown", weaponType.getCooldown());
                    fields.put("currentCooldown", weaponType.getAnimationAndFocusCooldown());
                    fields.put("reach", rangedAttackComponent.range);
                    fields.put("horizontalRotation", rangedAttackComponent.horizontalRotation);
                    fields.put("verticalRotation", rangedAttackComponent.verticalRotation);
                    ComponentSnapshot snap = new ComponentSnapshot("RangedAttackComponent", fields);
                    server.getUpdateTracker().markComponentModified(world.getEntity(e), snap);
                }else rangedAttackComponent.cooldown = weaponType.getAnimationAndFocusCooldown();
            }
            if (weaponType.getType().equals(WeaponType.Type.ProjectileLauncher)) {
                ProjectileAttackComponent projectileAttackComponent = (ProjectileAttackComponent) component;
                ComponentSnapshot previousSnapshot = server.getUpdateTracker().getPreviousSnapshot(world.getEntity(e), "RangedAttackComponent");
                if (previousSnapshot != null) {
                    previousSnapshot.getFields().put("currentCooldown", weaponType.getAnimationAndFocusCooldown());
                } else if(weaponType.getAnimationAndFocusCooldown() > projectileAttackComponent.cooldown) {
                    HashMap<String, Object> fields = new HashMap<>();
                    fields.put("weaponType", weaponType);
                    fields.put("cooldown", weaponType.getCooldown());
                    fields.put("currentCooldown", weaponType.getAnimationAndFocusCooldown());
                    fields.put("range", projectileAttackComponent.range);
                    fields.put("EntityType", projectileAttackComponent.projectileType);
                    fields.put("horizontalRotation", projectileAttackComponent.horizontalRotation);
                    fields.put("verticalRotation", projectileAttackComponent.verticalRotation);
                    ComponentSnapshot snap = new ComponentSnapshot("ProjectileAttackComponent", fields);
                    server.getUpdateTracker().markComponentModified(world.getEntity(e), snap);
                }else projectileAttackComponent.cooldown = weaponType.getAnimationAndFocusCooldown();
            }
        }
    }
}
