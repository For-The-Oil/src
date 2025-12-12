package io.github.server.game_engine.system;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.annotations.Wire;
import com.artemis.systems.IteratingSystem;
import com.artemis.utils.IntBag;

import java.util.ArrayList;
import java.util.HashMap;

import io.github.server.data.ServerGame;
import io.github.shared.data.component.BuildingMapPositionComponent;
import io.github.shared.data.component.FreezeComponent;
import io.github.shared.data.component.LifeComponent;
import io.github.shared.data.component.MeleeAttackComponent;
import io.github.shared.data.component.MoveComponent;
import io.github.shared.data.component.NetComponent;
import io.github.shared.data.component.OnCreationComponent;
import io.github.shared.data.component.PositionComponent;
import io.github.shared.data.component.ProprietyComponent;
import io.github.shared.data.component.TargetComponent;
import io.github.shared.data.component.VelocityComponent;
import io.github.shared.data.enums_types.EntityType;
import io.github.shared.data.gameobject.DamageEntry;
import io.github.shared.data.snapshot.ComponentSnapshot;
import io.github.shared.shared_engine.Utility;
import io.github.shared.shared_engine.manager.EcsManager;

/**
 * Melee Attack System — strict order and server-friendly updates
 *
 * Sequence:
 *  1) Check PRIMARY target reachability (TargetComponent.targetId).
 *  2) If not in reach, check SECONDARY target reachability (TargetComponent.nextTargetId).
 *  3) If NOT forced (TargetComponent.force == false) and no candidate yet, scan "enemies around"
 *     by finding the nearest enemy within the attacker's reach.
 *  4) Cooldown logic:
 *     - If an attackable enemy is found AND cooldown is ready -> create a DamageComponent snapshot
 *       (with a single DamageEntry) via SnapshotTracker, then reset cooldown to (cooldown + animationAndFocusCooldown).
 *     - Else -> if no attackable entity was found, raise currentCooldown to at least animationAndFocusCooldown;
 *       otherwise tick down normally.
 *
 * Notes:
 *  - No direct modification of the target's DamageComponent here. We register a ComponentSnapshot with
 *    ServerGame's SnapshotTracker. The tracker merges per-entity diffs and will be consumed later to build
 *    a single UpdateEntityInstruction for the frame.
 */
@Wire
public class MeleeAttackSystem extends IteratingSystem {

    // Component mappers injected by Artemis-ODB
    private ComponentMapper<MeleeAttackComponent> mMelee;
    private ComponentMapper<PositionComponent> mPos;
    private ComponentMapper<BuildingMapPositionComponent> bPos;
    private ComponentMapper<ProprietyComponent> mProp;
    private ComponentMapper<TargetComponent> mTarget;
    private ComponentMapper<NetComponent> mNet;
    private ComponentMapper<MoveComponent> mMove;
    private ComponentMapper<OnCreationComponent> mOnCreation;
    private ComponentMapper<VelocityComponent> mVel;

    // Server needed to access the SnapshotTracker (aggregation of component snapshots)
    private final ServerGame server;
    final float EPS = 1e-4f;

    /**
     * Constructor requires the ServerGame instance so we can register snapshots.
     */
    public MeleeAttackSystem(ServerGame server) {
        super(Aspect.all(MeleeAttackComponent.class, PositionComponent.class, ProprietyComponent.class,NetComponent.class).exclude(FreezeComponent.class, OnCreationComponent.class));
        this.server = server;
    }

    @Override
    protected void process(int e) {
        // Attacker components
        MeleeAttackComponent melee = mMelee.get(e);// provides reach, cooldown fields and helpers
        PositionComponent pos = mPos.get(e);// 3D position (x,y,z)
        ProprietyComponent meP = mProp.get(e);// team ownership
        TargetComponent tgt = mTarget.get(e); // primary/secondary targets + forced flag
        MoveComponent move = mMove.get(e);
        NetComponent net = mNet.get(e);// netId
        VelocityComponent vel = mVel.get(e);
        float time = melee.currentCooldown - world.getDelta();
        float tmp = melee.horizontalRotation;
        boolean foundAttackable = false;

        if(vel == null || vel.isStop() || melee.weaponType.isHitAndMove()) {
            // Precompute squared reach for distance checks (avoids sqrt)
            final float reach2 = melee.reach * melee.reach;

            // (1) PRIMARY target: is it within melee reach?
            int candidateNetId = -1;
            boolean inReach = false;
            boolean isTypeBuilding = false;
            float BuildingPosx = -1;
            float BuildingPosy = -1;

            if (tgt != null && tgt.hasTarget()) {
                int te = EcsManager.getIdByNetId(world, tgt.targetNetId, mNet);
                PositionComponent tPos = mPos.get(te);
                NetComponent tnet = mNet.get(te);
                if (tnet != null && tnet.entityType.getType().equals(EntityType.Type.Building)) {
                    BuildingMapPositionComponent bp = bPos.get(te);
                    ArrayList<Float> arrayList = Utility.isAttackValidForBuilding(pos, tPos, melee.reach, tnet.entityType.getShapeType(), bp.direction);
                    if (!arrayList.isEmpty()) {
                        BuildingPosx = arrayList.get(0);
                        BuildingPosy = arrayList.get(1);
                        candidateNetId = tgt.targetNetId;
                        inReach = true;
                        isTypeBuilding = true;
                    }
                } else if (tPos != null) {
                    float dx = tPos.x - pos.x;
                    float dy = tPos.y - pos.y;
                    float dz = tPos.z - pos.z;
                    float dist2 = dx * dx + dy * dy + dz * dz;
                    if (dist2 <= reach2) {
                        candidateNetId = tgt.targetNetId;
                        inReach = true;
                    } else if (move == null || (!move.force && !move.targetRelated)) {
                        HashMap<String, Object> fields = new HashMap<>();
                        fields.put("targetRelated", true);
                        fields.put("destinationX", -1);
                        fields.put("destinationY", -1);
                        fields.put("force", false);
                        ComponentSnapshot moveComponent = new ComponentSnapshot("MoveComponent", fields);
                        server.getUpdateTracker().markComponentModified(world.getEntity(e), moveComponent);
                    }
                }
            }

            // (2) SECONDARY target: only if primary is not in reach
            if (!inReach && tgt != null && tgt.hasNextTarget()) {
                int te2 = EcsManager.getIdByNetId(world, tgt.targetNetId, mNet);
                PositionComponent tPos2 = mPos.get(te2);
                NetComponent tnet2 = mNet.get(te2);
                if (tnet2 != null && tnet2.entityType.getType().equals(EntityType.Type.Building)) {
                    BuildingMapPositionComponent bp = bPos.get(te2);
                    ArrayList<Float> arrayList = Utility.isAttackValidForBuilding(pos, tPos2, melee.reach, tnet2.entityType.getShapeType(), bp.direction);
                    if (!arrayList.isEmpty()) {
                        BuildingPosx = arrayList.get(0);
                        BuildingPosy = arrayList.get(1);
                        candidateNetId = tgt.nextTargetId;
                        inReach = true;
                        isTypeBuilding = true;
                    }
                } else if (tPos2 != null) {
                    float dx = tPos2.x - pos.x;
                    float dy = tPos2.y - pos.y;
                    float dz = tPos2.z - pos.z;
                    float dist2 = dx * dx + dy * dy + dz * dz;
                    if (dist2 <= reach2) {
                        candidateNetId = tgt.nextTargetId;
                        inReach = true;
                    }
                }
            }

            // Forced flag from target component: if true, we must NOT switch to "around" enemies
            final boolean forced = (tgt != null && tgt.force);

            // (3) ENEMIES AROUND — nearest enemy within reach
            // Only if NOT forced and we still have no candidate in reach.

            if (!forced && !inReach) {
                IntBag bag = world.getAspectSubscriptionManager().get(Aspect.all(PositionComponent.class, ProprietyComponent.class, LifeComponent.class)).getEntities();
                int[] ids = bag.getData();
                float bestDist2 = Float.MAX_VALUE;
                for (int i = 0, n = bag.size(); i < n; i++) {
                    int other = ids[i];
                    if (other == e) continue; // skip self
                    // Must be an enemy (different team)
                    ProprietyComponent oP = mProp.get(other);
                    if (oP == null || meP == null || oP.team == null || meP.team == null) continue;
                    if (oP.team.equals(meP.team)) continue; // skip allies
                    NetComponent onet = mNet.get(other);
                    if (mOnCreation.get(e) != null && onet != null && !onet.entityType.getType().equals(EntityType.Type.Building))
                        continue;
                    if (onet != null && onet.entityType.getType().equals(EntityType.Type.Building)) {
                        PositionComponent oPos = mPos.get(other);
                        BuildingMapPositionComponent bp = bPos.get(other);
                        ArrayList<Float> arrayList = Utility.isAttackValidForBuilding(pos, oPos, melee.reach, onet.entityType.getShapeType(), bp.direction);
                        if (!arrayList.isEmpty()) {
                            BuildingPosx = arrayList.get(0);
                            BuildingPosy = arrayList.get(1);
                            float dx = arrayList.get(0) - pos.x;
                            float dy = arrayList.get(1) - pos.y;
                            float dz = oPos.z - pos.z;
                            float dist2 = dx * dx + dy * dy + dz * dz;
                            bestDist2 = dist2;
                            candidateNetId = onet.netId;
                            inReach = true;
                            isTypeBuilding = true;
                        }
                    } else if (onet != null) {
                        PositionComponent oPos = mPos.get(other);
                        if (oPos == null) continue;

                        float dx = oPos.x - pos.x;
                        float dy = oPos.y - pos.y;
                        float dz = oPos.z - pos.z;
                        float dist2 = dx * dx + dy * dy + dz * dz;

                        // Accept enemies within reach and keep the nearest one
                        if (dist2 <= reach2 && dist2 < bestDist2) {
                            bestDist2 = dist2;
                            candidateNetId = onet.netId;
                            inReach = true; // by construction this "around" enemy is within reach
                            isTypeBuilding = false;
                        }
                    }
                }
            }

            // (4) Cooldown + final attack decision
            final boolean ready = melee.isReady(); // cooldown ready now?

            // Friendly-fire prevention: ensure candidate is an enemy (different team)
            boolean isEnemy = false;
            if (candidateNetId != -1) {
                ProprietyComponent tP = mProp.get(EcsManager.getIdByNetId(world, candidateNetId, mNet));
                isEnemy = (tP == null || meP == null || tP.team == null || meP.team == null || !tP.team.equals(meP.team));
            }
            // We can attack only if we have a valid enemy candidate in reach and cooldown is ready
            foundAttackable = (candidateNetId != -1 && inReach && isEnemy);
            if (foundAttackable) {
                float tPosx = -1;
                float tPosy = -1;
                PositionComponent tPos = mPos.get(EcsManager.getIdByNetId(world, candidateNetId, mNet));
                if (isTypeBuilding) {
                    tPosx = BuildingPosx;
                    tPosy = BuildingPosy;
                } else if (tPos != null) {
                    tPosx = tPos.x;
                    tPosy = tPos.y;
                }
                if (tPosx != -1 && tPosy != -1) {
                    float dx = tPosx - pos.x;
                    float dy = tPosy - pos.y;
                    float rawTarget = (float) Math.atan2(-dy, dx);
                    float facingOffset = (float) -Math.PI / 2f;
                    float target = Utility.normAngle(rawTarget + facingOffset);

                    if (melee.weaponType.isTurret() && (Math.abs(Utility.normAngle(target - melee.horizontalRotation)) > EPS)) {
                        tmp = target;
                        ComponentSnapshot previousSnapshot = server.getUpdateTracker().getPreviousSnapshot(world.getEntity(e), "MeleeAttackComponent");
                        if (previousSnapshot != null) {
                            previousSnapshot.getFields().put("horizontalRotation", tmp);
                        } else {
                            HashMap<String, Object> fields = new HashMap<>();
                            fields.put("weaponType", melee.weaponType);
                            fields.put("damage", melee.damage);
                            fields.put("cooldown", melee.cooldown);
                            fields.put("currentCooldown", melee.currentCooldown);
                            fields.put("reach", melee.reach);
                            fields.put("horizontalRotation", tmp);
                            fields.put("verticalRotation", melee.verticalRotation);
                            ComponentSnapshot positionComponent = new ComponentSnapshot("MeleeAttackComponent", fields);
                            server.getUpdateTracker().markComponentModified(world.getEntity(e), positionComponent);
                        }
                    }
                    if (!melee.weaponType.isHitAndMove() && (Math.abs(Utility.normAngle(target - pos.horizontalRotation)) > EPS)) {
                        ComponentSnapshot previousSnapshot2 = server.getUpdateTracker().getPreviousSnapshot(world.getEntity(e), "PositionComponent");
                        if (previousSnapshot2 != null) {
                            previousSnapshot2.getFields().put("horizontalRotation", target);
                        } else {
                            HashMap<String, Object> fields = new HashMap<>();
                            fields.put("x", pos.x);
                            fields.put("y", pos.y);
                            fields.put("z", pos.z);
                            fields.put("horizontalRotation", target);
                            fields.put("verticalRotation", pos.verticalRotation);
                            ComponentSnapshot positionComponent2 = new ComponentSnapshot("PositionComponent", fields);
                            server.getUpdateTracker().markComponentModified(world.getEntity(e), positionComponent2);
                        }
                    }
                }
            }


            if (ready && foundAttackable) {
                // Perform attack via SnapshotTracker (no direct component mutation on the target)

                // Read weapon metadata: armor penetration and extra animation/focus cooldown
                float armorPen = melee.weaponType.getArmorPenetration();

                // Build a DamageComponent snapshot that appends a single DamageEntry to the target.
                // SnapshotTracker knows how to merge "entries" by concatenation (server-side aggregation).
                java.util.ArrayList<Object> entries = new java.util.ArrayList<>();
                entries.add(new DamageEntry(net.netId, melee.damage, armorPen)); // attackerId, raw damage, armor penetration

                java.util.HashMap<String, Object> fields = new java.util.HashMap<>();
                fields.put("entries", entries); // tracker will merge lists of entries per target

                ComponentSnapshot damageSnap = new ComponentSnapshot("DamageComponent", fields);

                // Register this component change for the target.
                // SnapshotTracker resolves NetComponent (netId, entityType) and aggregates per-entity snapshots.
                server.getUpdateTracker().markComponentModified(world.getEntity(EcsManager.getIdByNetId(world, candidateNetId, mNet)), damageSnap);

                // Locally finalize the attack by resetting the cooldown
                time = melee.weaponType.getCooldown();

            } else if (!foundAttackable) {
                // No attack performed this frame
                // If NO attackable entity was found at all, raise cooldown to at least animation/focus threshold.
                // Otherwise, tick down normally.
                float extra = melee.weaponType.getAnimationAndFocusCooldown();
                if (melee.currentCooldown < extra) {
                    // Raise to the animation/focus minimum (explicit penalty for not having a target)
                    time = extra;
                    // Intentionally do NOT tick down this frame.
                }
            }
        }
        else if(melee.weaponType.getAnimationAndFocusCooldown() >= melee.currentCooldown)return;
        if(!Utility.inSameCooldownBand(melee.currentCooldown,time,melee.weaponType.getAnimationCooldown(),melee.weaponType.getAnimationAndFocusCooldown())) {
            ComponentSnapshot previousSnapshot = server.getUpdateTracker().getPreviousSnapshot(world.getEntity(e),"MeleeAttackComponent");
            if(previousSnapshot != null){
                previousSnapshot.getFields().put("currentCooldown",time);
            }
            else {
                java.util.HashMap<String, Object> fields = new java.util.HashMap<>();
                fields.put("weaponType", melee.weaponType);
                fields.put("damage", melee.damage);
                fields.put("cooldown", melee.cooldown);
                fields.put("currentCooldown", time);
                fields.put("reach", melee.reach);
                fields.put("horizontalRotation", tmp);
                fields.put("verticalRotation", melee.verticalRotation);
                ComponentSnapshot damageSnap = new ComponentSnapshot("MeleeAttackComponent", fields);
                server.getUpdateTracker().markComponentModified(world.getEntity(e), damageSnap);
            }
        } else if(foundAttackable || melee.currentCooldown != melee.weaponType.getAnimationAndFocusCooldown())melee.updateCooldown(world.getDelta());
    }

}
