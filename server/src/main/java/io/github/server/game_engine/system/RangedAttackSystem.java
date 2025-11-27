package io.github.server.game_engine.system;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.annotations.Wire;
import com.artemis.systems.IteratingSystem;
import com.artemis.utils.IntBag;

import io.github.server.data.ServerGame;
import io.github.shared.data.component.FreezeComponent;
import io.github.shared.data.component.LifeComponent;
import io.github.shared.data.component.NetComponent;
import io.github.shared.data.component.OnCreationComponent;
import io.github.shared.data.component.PositionComponent;
import io.github.shared.data.component.ProprietyComponent;
import io.github.shared.data.component.RangedAttackComponent;
import io.github.shared.data.component.TargetComponent;
import io.github.shared.data.gameobject.Cell;
import io.github.shared.data.gameobject.DamageEntry;
import io.github.shared.data.gameobject.Shape;
import io.github.shared.data.snapshot.ComponentSnapshot;
import io.github.shared.shared_engine.Utility;

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
    private ComponentMapper<ProprietyComponent> mProp;
    private ComponentMapper<TargetComponent> mTarget;

    private ComponentMapper<NetComponent> mNet;

    // Server handle to reach SnapshotTracker for aggregated updates
    private final ServerGame server;

    /**
     * Requires ServerGame to register snapshots each frame.
     */
    public RangedAttackSystem(ServerGame server) {
        super(Aspect.all(RangedAttackComponent.class, PositionComponent.class, ProprietyComponent.class, TargetComponent.class,NetComponent.class).exclude(FreezeComponent.class, OnCreationComponent.class));
        this.server = server;
    }

    @Override
    protected void process(int e) {

        // Attacker state
        RangedAttackComponent ranged = mRanged.get(e); // provides range & cooldown helpers
        PositionComponent pos = mPos.get(e);    // attacker's world position
        ProprietyComponent meP = mProp.get(e);   // team (used to avoid friendly fire)
        TargetComponent tgt = mTarget.get(e); // primary/secondary targets + forced flag
        NetComponent net = mNet.get(e);// netId
        float time = ranged.currentCooldown-world.getDelta();

        // (1) PRIMARY target — validate via the specialized range function
        int candidateId = -1;
        boolean inRange = false;

        if (tgt != null && tgt.hasTarget()) {
            PositionComponent tPos = mPos.get(tgt.targetId);
            // Placeholder function: replace with your real range/LoS/ballistics check
            if (tPos != null && Utility.isRangedDistanceValid(pos, tPos, ranged.range, server.getMap())) {
                candidateId = tgt.targetId;
                inRange = true;
            }
        }

        // (2) SECONDARY target — only if primary is not valid/in range
        if (!inRange && tgt != null && tgt.hasNextTarget()) {
            PositionComponent tPos2 = mPos.get(tgt.nextTargetId);
            if (tPos2 != null && Utility.isRangedDistanceValid(pos, tPos2, ranged.range, server.getMap())) {
                candidateId = tgt.nextTargetId;
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

                PositionComponent oPos = mPos.get(other);
                if (oPos == null) continue;

                // Validate via your specialized check
                if (Utility.isRangedDistanceValid(pos, oPos, ranged.range, server.getMap())) {
                    // Choose nearest valid enemy (using squared distance)
                    float dx = oPos.x - pos.x, dy = oPos.y - pos.y, dz = oPos.z - pos.z;
                    float dist2 = dx*dx + dy*dy + dz*dz;
                    if (dist2 < bestScore) {
                        bestScore = dist2;
                        candidateId = other;
                        inRange = true;
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
        if (candidateId != -1) {
            ProprietyComponent tP = mProp.get(candidateId);
            isEnemy = (tP == null || meP == null || tP.team == null || meP.team == null || !tP.team.equals(meP.team));
        }

        // Attack is permitted only when we have a valid enemy candidate in range and cooldown is ready
        final boolean foundAttackable = (candidateId != -1 && inRange && isEnemy);

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
            server.getUpdateTracker().markComponentModified(world.getEntity(candidateId), damageSnap);

            // Reset cooldown: base ranged cooldown + animation/focus extra
            time = ranged.weaponType.getCooldown();
        }
         else {
                // No attack performed this frame
                // If NO attackable entity was found at all, raise cooldown to at least animation/focus threshold.
                // Otherwise, tick down normally.

                float extra = ranged.weaponType.getAnimationAndFocusCooldown();

                boolean noEntityFound = (candidateId == -1); // nothing in reach after all checks
                if (noEntityFound && ranged.currentCooldown < extra) {
                    // Raise to the animation/focus minimum (explicit penalty for not having a target)
                    time = extra;
                    // Intentionally do NOT tick down this frame.
                }
            }
            if(time!=0f){
                java.util.HashMap<String, Object> fields = new java.util.HashMap<>();
                fields.put("weaponType",ranged.weaponType );
                fields.put("damage",ranged.damage );
                fields.put("cooldown",ranged.cooldown );
                fields.put("currentCooldown",time);
                fields.put("reach",ranged.range );
                ComponentSnapshot damageSnap = new ComponentSnapshot("MeleeAttackComponent", fields);
                server.getUpdateTracker().markComponentModified(world.getEntity(e), damageSnap);
            }
    }
}

