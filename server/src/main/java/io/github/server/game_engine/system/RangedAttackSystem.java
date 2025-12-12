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
import io.github.shared.data.component.MoveComponent;
import io.github.shared.data.component.NetComponent;
import io.github.shared.data.component.OnCreationComponent;
import io.github.shared.data.component.PositionComponent;
import io.github.shared.data.component.ProprietyComponent;
import io.github.shared.data.component.RangedAttackComponent;
import io.github.shared.data.component.TargetComponent;
import io.github.shared.data.enums_types.EntityType;
import io.github.shared.data.gameobject.DamageEntry;
import io.github.shared.data.snapshot.ComponentSnapshot;
import io.github.shared.shared_engine.Utility;
import io.github.shared.shared_engine.manager.EcsManager;

/**
 * Ranged Attack System — strict sequence with server-side snapshotting
 *
 * Steps:
 *  1) Try PRIMARY target: validate ranged distance via isRangedDistanceValid(...).
 *  2) If not valid, try SECONDARY target: same validation function.
 *  3) If NOT forced and still no candidate, find "enemies around":
 *     scan all enemies and choose the nearest one for which isRangedDistanceValid(...) is true.
 *  4) Cooldown handling:
 *     - If we found a valid attack target AND cooldown is ready:
 *       register a DamageComponent snapshot (one DamageEntry) with ServerGame's SnapshotTracker,
 *       then reset cooldown to (base cooldown + animation/focus extra).
 *     - Else:
 *       if no attackable entity was found, raise currentCooldown to at least animation/focus extra;
 *       otherwise, tick cooldown down normally.
 *
 * Design notes:
 *  - We do NOT mutate the target's DamageComponent directly. Instead, we emit a ComponentSnapshot
 *    through SnapshotTracker, which aggregates per-entity changes and later produces a single UpdateEntityInstruction.
 */
@Wire
public class RangedAttackSystem extends IteratingSystem {

    // Artemis component mappers
    private ComponentMapper<RangedAttackComponent> mRanged;
    private ComponentMapper<PositionComponent> mPos;
    private ComponentMapper<BuildingMapPositionComponent> bPos;
    private ComponentMapper<ProprietyComponent> mProp;
    private ComponentMapper<TargetComponent> mTarget;
    private ComponentMapper<NetComponent> mNet;
    private ComponentMapper<MoveComponent> mMove;


    // Server handle to reach SnapshotTracker for aggregated updates
    private final ServerGame server;
    final float EPS = 1e-4f;

    /**
     * Requires ServerGame to register snapshots each frame.
     */
    public RangedAttackSystem(ServerGame server) {
        super(Aspect.all(RangedAttackComponent.class, PositionComponent.class, ProprietyComponent.class,NetComponent.class).exclude(FreezeComponent.class, OnCreationComponent.class));
        this.server = server;
    }

    @Override
    protected void process(int e) {

        // Attacker state
        RangedAttackComponent ranged = mRanged.get(e); // provides range & cooldown helpers
        PositionComponent pos = mPos.get(e);    // attacker's world position
        ProprietyComponent meP = mProp.get(e);   // team (used to avoid friendly fire)
        TargetComponent tgt = mTarget.get(e); // primary/secondary targets + forced flag
        MoveComponent move = mMove.get(e);
        NetComponent net = mNet.get(e);// netId
        float time = ranged.currentCooldown-world.getDelta();
        float tmp = ranged.horizontalRotation;

        // (1) PRIMARY target — validate via the specialized range function
        int candidateNetId = -1;
        boolean inRange = false;
        boolean isTypeBuilding = false;
        float BuildingPosx = -1;
        float BuildingPosy = -1;

        if (tgt != null && tgt.hasTarget()) {
            int te = EcsManager.getIdByNetId(world,tgt.targetNetId, mNet);
            PositionComponent tPos = mPos.get(te);
            NetComponent tnet = mNet.get(te);
            if(tnet != null && tnet.entityType.getType().equals(EntityType.Type.Building)){
                BuildingMapPositionComponent bp = bPos.get(te);
                ArrayList<Float> arrayList = Utility.isRangedDistanceValidForBuilding(pos,tPos,ranged.range,server.getMap(),tnet.entityType.getShapeType(),bp.direction);
                if(!arrayList.isEmpty()) {
                    BuildingPosx = arrayList.get(0);
                    BuildingPosy = arrayList.get(1);
                    candidateNetId = tgt.targetNetId;
                    inRange = true;
                    isTypeBuilding = true;
                }
            }
            else if (tPos != null){
                if(Utility.isRangedDistanceValid(pos, tPos, ranged.range, server.getMap())) {
                    candidateNetId = tgt.targetNetId;
                    inRange = true;
                }
                else if(move == null|| (!move.force && !move.targetRelated)){
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

        // (2) SECONDARY target — only if primary is not valid/in range
        if (!inRange && tgt != null && tgt.hasNextTarget()) {
            int te2 = EcsManager.getIdByNetId(world,tgt.targetNetId, mNet);
            PositionComponent tPos2 = mPos.get(te2);
            NetComponent tnet2 = mNet.get(te2);
            if(tnet2 != null && tnet2.entityType.getType().equals(EntityType.Type.Building)){
                BuildingMapPositionComponent bp = bPos.get(te2);
                ArrayList<Float> arrayList = Utility.isRangedDistanceValidForBuilding(pos,tPos2,ranged.range,server.getMap(),tnet2.entityType.getShapeType(),bp.direction);
                if(!arrayList.isEmpty()) {
                    BuildingPosx = arrayList.get(0);
                    BuildingPosy = arrayList.get(1);
                    candidateNetId = tgt.nextTargetId;
                    inRange = true;
                    isTypeBuilding = true;
                }
            }
            else if (tPos2 != null && Utility.isRangedDistanceValid(pos, tPos2, ranged.range, server.getMap())) {
                candidateNetId = tgt.nextTargetId;
                inRange = true;
            }
        }

        // Forced flag: when true, we must NOT switch to "around" enemies
        final boolean forced = (tgt != null && tgt.force);


        // (3) ENEMIES AROUND — nearest enemy passing the validity function
        // Only if NOT forced and we still have no candidate.
        if (!forced && !inRange) {
            IntBag bag = world.getAspectSubscriptionManager().get(Aspect.all(PositionComponent.class, ProprietyComponent.class, LifeComponent.class)).getEntities();
            int[] ids = bag.getData();
            float bestScore = Float.MAX_VALUE; // selection metric (here: squared distance)

            for (int i = 0, n = bag.size(); i < n; i++) {
                int other = ids[i];
                if (other == e) continue; // ignore self

                // Must be an enemy (team differs)
                ProprietyComponent oP = mProp.get(other);
                if (oP == null || meP == null || oP.team == null || meP.team == null) continue;
                if (oP.team.equals(meP.team)) continue; // skip allies
                NetComponent onet = mNet.get(other);
                if(onet != null && onet.entityType.getType().equals(EntityType.Type.Building)){
                    PositionComponent oPos = mPos.get(other);
                    BuildingMapPositionComponent bp = bPos.get(other);
                    ArrayList<Float> arrayList = Utility.isRangedDistanceValidForBuilding(pos,oPos,ranged.range,server.getMap(),onet.entityType.getShapeType(),bp.direction);
                    if(!arrayList.isEmpty()) {
                        BuildingPosx = arrayList.get(0);
                        BuildingPosy = arrayList.get(1);
                        float dx = arrayList.get(0) - pos.x;
                        float dy = arrayList.get(1) - pos.y;
                        float dz = oPos.z - pos.z;
                        float dist2 = dx * dx + dy * dy + dz * dz;
                        bestScore = dist2;
                        candidateNetId = onet.netId;
                        inRange = true;
                        isTypeBuilding = true;
                    }
                }
                else if(onet != null) {
                    PositionComponent oPos = mPos.get(other);
                    if (oPos == null) continue;

                    // Validate via your specialized check
                    if (Utility.isRangedDistanceValid(pos, oPos, ranged.range, server.getMap())) {
                        // Choose nearest valid enemy (using squared distance)
                        float dx = oPos.x - pos.x, dy = oPos.y - pos.y, dz = oPos.z - pos.z;
                        float dist2 = dx * dx + dy * dy + dz * dz;
                        if (dist2 < bestScore) {
                            bestScore = dist2;
                            candidateNetId = onet.netId;
                            inRange = true;
                        }
                    }
                }
            }
        }

        // ------------------------------------------------------------
        // (4) Cooldown + final attack decision
        // ------------------------------------------------------------
        final boolean ready = ranged.isReady(); // cooldown ready this frame?

        // Prevent friendly fire: ensure candidate is an enemy
        boolean isEnemy = false;
        if (candidateNetId != -1) {
            ProprietyComponent tP = mProp.get(EcsManager.getIdByNetId(world,candidateNetId,mNet));
            isEnemy = (tP == null || meP == null || tP.team == null || meP.team == null || !tP.team.equals(meP.team));
        }

        // Attack is permitted only when we have a valid enemy candidate in range and cooldown is ready
        final boolean foundAttackable = (candidateNetId != -1 && inRange && isEnemy);
        if(foundAttackable){
            float tPosx = -1;
            float tPosy = -1;
            PositionComponent tPos = mPos.get(EcsManager.getIdByNetId(world,candidateNetId,mNet));
            if(isTypeBuilding){
                tPosx = BuildingPosx;
                tPosy = BuildingPosy;
            }
            else if(tPos!=null) {
                tPosx = tPos.x;
                tPosy = tPos.y;
            }
            if (tPosx != -1 && tPosy != -1) {
                float dx = tPosx - pos.x;
                float dy = tPosy - pos.y;
                float rawTarget = (float) Math.atan2(dy, dx);
                float target = Utility.normAngle((float) (rawTarget + Math.PI));

                if (ranged.weaponType.isTurret() && (Math.abs(Utility.normAngle(target - ranged.horizontalRotation)) > EPS)) {
                    tmp = target;
                    ComponentSnapshot previousSnapshot = server.getUpdateTracker().getPreviousSnapshot(world.getEntity(e), "RangedAttackComponent");
                    if (previousSnapshot != null) {
                        previousSnapshot.getFields().put("horizontalRotation", tmp);
                    } else {
                        HashMap<String, Object> fields = new HashMap<>();
                        fields.put("weaponType", ranged.weaponType);
                        fields.put("damage", ranged.damage);
                        fields.put("cooldown", ranged.cooldown);
                        fields.put("currentCooldown", ranged.currentCooldown);
                        fields.put("range", ranged.range);
                        fields.put("horizontalRotation", tmp);
                        fields.put("verticalRotation", ranged.verticalRotation);
                        ComponentSnapshot positionComponent = new ComponentSnapshot("RangedAttackComponent", fields);
                        server.getUpdateTracker().markComponentModified(world.getEntity(e), positionComponent);
                    }
                }
                if (!ranged.weaponType.isHitAndMove() && (Math.abs(Utility.normAngle(target - pos.horizontalRotation)) > EPS)) {
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
            // === Perform ranged attack via SnapshotTracker (no direct writes to target components) ===

            // Read weapon metadata for damage and cooldown adjustments
            float armorPen = ranged.weaponType.getArmorPenetration();

            // Build a DamageComponent snapshot that appends one DamageEntry
            java.util.ArrayList<Object> entries = new java.util.ArrayList<>();
            entries.add(new DamageEntry(net.netId, ranged.damage, armorPen)); // (attackerId, raw damage, AP)

            java.util.HashMap<String, Object> fields = new java.util.HashMap<>();
            fields.put("entries", entries); // SnapshotTracker merges "entries" by concatenation

            ComponentSnapshot damageSnap = new ComponentSnapshot("DamageComponent", fields);

            // Register snapshot for the target; aggregated later into a single UpdateEntityInstruction
            server.getUpdateTracker().markComponentModified(world.getEntity(EcsManager.getIdByNetId(world,candidateNetId,mNet)), damageSnap);

            // Reset cooldown: base ranged cooldown + animation/focus extra
            time = ranged.weaponType.getCooldown();
        }
         else if(!foundAttackable) {
                // No attack performed this frame
                // If NO attackable entity was found at all, raise cooldown to at least animation/focus threshold.
                // Otherwise, tick down normally.

                float extra = ranged.weaponType.getAnimationAndFocusCooldown();
                if (ranged.currentCooldown < extra) {
                    // Raise to the animation/focus minimum (explicit penalty for not having a target)
                    time = extra;
                }
            }

        if(!Utility.inSameCooldownBand(ranged.currentCooldown,time,ranged.weaponType.getAnimationCooldown(),ranged.weaponType.getAnimationAndFocusCooldown())) {
            ComponentSnapshot previousSnapshot = server.getUpdateTracker().getPreviousSnapshot(world.getEntity(e),"RangedAttackComponent");
            if(previousSnapshot != null){
                previousSnapshot.getFields().put("currentCooldown",time);
            }
            else {
                java.util.HashMap<String, Object> fields = new java.util.HashMap<>();
                fields.put("weaponType", ranged.weaponType);
                fields.put("damage", ranged.damage);
                fields.put("cooldown", ranged.cooldown);
                fields.put("currentCooldown", time);
                fields.put("reach", ranged.range);
                fields.put("horizontalRotation", tmp);
                fields.put("verticalRotation", ranged.verticalRotation);
                ComponentSnapshot damageSnap = new ComponentSnapshot("RangedAttackComponent", fields);
                server.getUpdateTracker().markComponentModified(world.getEntity(e), damageSnap);
            }
        }else if(foundAttackable || ranged.currentCooldown != ranged.weaponType.getAnimationAndFocusCooldown())ranged.updateCooldown(world.getDelta());
    }
}

