package io.github.server.game_engine.system;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.annotations.Wire;
import com.artemis.systems.IteratingSystem;
import com.artemis.utils.IntBag;

import java.util.HashMap;

import io.github.server.data.ServerGame;
import io.github.shared.data.component.FreezeComponent;
import io.github.shared.data.component.LifeComponent;
import io.github.shared.data.component.MeleeAttackComponent;
import io.github.shared.data.component.NetComponent;
import io.github.shared.data.component.OnCreationComponent;
import io.github.shared.data.component.PositionComponent;
import io.github.shared.data.component.ProprietyComponent;
import io.github.shared.data.component.TargetComponent;
import io.github.shared.data.gameobject.DamageEntry;
import io.github.shared.data.snapshot.ComponentSnapshot;

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
    private ComponentMapper<ProprietyComponent> mProp;
    private ComponentMapper<TargetComponent> mTarget;
    private ComponentMapper<NetComponent> mNet;

    // Server needed to access the SnapshotTracker (aggregation of component snapshots)
    private final ServerGame server;

    /**
     * Constructor requires the ServerGame instance so we can register snapshots.
     */
    public MeleeAttackSystem(ServerGame server) {
        super(Aspect.all(MeleeAttackComponent.class, PositionComponent.class, ProprietyComponent.class, TargetComponent.class,NetComponent.class).exclude(FreezeComponent.class, OnCreationComponent.class));
        this.server = server;
    }

    @Override
    protected void process(int e) {

        // Attacker components
        MeleeAttackComponent melee = mMelee.get(e);// provides reach, cooldown fields and helpers
        PositionComponent pos = mPos.get(e);// 3D position (x,y,z)
        ProprietyComponent meP = mProp.get(e);// team ownership
        TargetComponent tgt = mTarget.get(e); // primary/secondary targets + forced flag
        NetComponent net = mNet.get(e);// netId
        float time = melee.currentCooldown-world.getDelta();

        // Precompute squared reach for distance checks (avoids sqrt)
        final float reach2 = melee.reach * melee.reach;

        // (1) PRIMARY target: is it within melee reach?
        int candidateId = -1;
        boolean inReach = false;

        if (tgt != null && tgt.hasTarget()) {
            PositionComponent tPos = mPos.get(tgt.targetId);
            if (tPos != null) {
                float dx = tPos.x - pos.x;
                float dy = tPos.y - pos.y;
                float dz = tPos.z - pos.z;
                float dist2 = dx*dx + dy*dy + dz*dz;
                if (dist2 <= reach2) {
                    candidateId = tgt.targetId;
                    inReach = true;
                }
            }
        }

        // (2) SECONDARY target: only if primary is not in reach
        if (!inReach && tgt != null && tgt.hasNextTarget()) {
            PositionComponent tPos2 = mPos.get(tgt.nextTargetId);
            if (tPos2 != null) {
                float dx = tPos2.x - pos.x;
                float dy = tPos2.y - pos.y;
                float dz = tPos2.z - pos.z;
                float dist2 = dx*dx + dy*dy + dz*dz;
                if (dist2 <= reach2) {
                    candidateId = tgt.nextTargetId;
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

                PositionComponent oPos = mPos.get(other);
                if (oPos == null) continue;

                float dx = oPos.x - pos.x;
                float dy = oPos.y - pos.y;
                float dz = oPos.z - pos.z;
                float dist2 = dx*dx + dy*dy + dz*dz;

                // Accept enemies within reach and keep the nearest one
                if (dist2 <= reach2 && dist2 < bestDist2) {
                    bestDist2 = dist2;
                    candidateId = other;
                    inReach = true; // by construction this "around" enemy is within reach
                }
            }
        }

        // (4) Cooldown + final attack decision
        final boolean ready = melee.isReady(); // cooldown ready now?

        // Friendly-fire prevention: ensure candidate is an enemy (different team)
        boolean isEnemy = false;
        if (candidateId != -1) {
            ProprietyComponent tP = mProp.get(candidateId);
            isEnemy = (tP == null || meP == null || tP.team == null || meP.team == null || !tP.team.equals(meP.team));
        }

        // We can attack only if we have a valid enemy candidate in reach and cooldown is ready
        final boolean foundAttackable = (candidateId != -1 && inReach && isEnemy);
        if(foundAttackable){
            PositionComponent tPos = mPos.get(candidateId);
            if (tPos != null) {
                float dx = tPos.x - (pos.x+melee.weaponType.getTranslationX());
                float dz = tPos.z - (pos.z+melee.weaponType.getTranslationZ());
                HashMap<String, Object> fields = new HashMap<>();
                fields.put("weaponType",melee.weaponType);
                fields.put("damage",melee.damage);
                fields.put("cooldown",melee.cooldown);
                fields.put("currentCooldown",melee.currentCooldown);
                fields.put("reach",melee.reach);
                fields.put("horizontalRotation", pos.horizontalRotation + ((float) Math.atan2(dz, dx) - pos.horizontalRotation) * melee.weaponType.getTurn_speed() * world.getDelta());
                fields.put("verticalRotation",melee.verticalRotation);
                ComponentSnapshot positionComponent = new ComponentSnapshot("MeleeAttackComponent", fields);
                server.getUpdateTracker().markComponentModified(world.getEntity(e), positionComponent);
                if (!melee.weaponType.isHitAndMove()){
                    dx = tPos.x - pos.x;
                    dz = tPos.z - pos.z;

                    fields = new HashMap<>();
                    fields.put("x", pos.x);
                    fields.put("y", pos.z);
                    fields.put("z", pos.z);
                    fields.put("horizontalRotation", (float) Math.atan2(dz, dx));
                    fields.put("verticalRotation", pos.verticalRotation);
                    ComponentSnapshot positionComponent2 = new ComponentSnapshot("PositionComponent", fields);
                    server.getUpdateTracker().markComponentModified(world.getEntity(e), positionComponent2);
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
            server.getUpdateTracker().markComponentModified(world.getEntity(candidateId), damageSnap);

            // Locally finalize the attack by resetting the cooldown
            time = melee.weaponType.getCooldown();

        }
        else {
            // No attack performed this frame
            // If NO attackable entity was found at all, raise cooldown to at least animation/focus threshold.
            // Otherwise, tick down normally.

            float extra = melee.weaponType.getAnimationAndFocusCooldown();

            boolean noEntityFound = (candidateId == -1); // nothing in reach after all checks
            if (noEntityFound && melee.currentCooldown < extra) {
                // Raise to the animation/focus minimum (explicit penalty for not having a target)
                time = extra;
                // Intentionally do NOT tick down this frame.
            }
        }
        if(time!=0f){
            java.util.HashMap<String, Object> fields = new java.util.HashMap<>();
            fields.put("weaponType",melee.weaponType );
            fields.put("damage",melee.damage );
            fields.put("cooldown",melee.cooldown );
            fields.put("currentCooldown",time);
            fields.put("reach",melee.reach );
            ComponentSnapshot damageSnap = new ComponentSnapshot("MeleeAttackComponent", fields);
            server.getUpdateTracker().markComponentModified(world.getEntity(e), damageSnap);
        }
    }
}
