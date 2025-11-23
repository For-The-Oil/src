package io.github.server.game_engine.system;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.annotations.Wire;
import com.artemis.systems.IteratingSystem;
import com.artemis.utils.IntBag;

import io.github.server.data.ServerGame;
import io.github.shared.data.EnumsTypes.WeaponType;
import io.github.shared.data.component.FreezeComponent;
import io.github.shared.data.component.LifeComponent;
import io.github.shared.data.component.NetComponent;
import io.github.shared.data.component.OnCreationComponent;
import io.github.shared.data.component.PositionComponent;
import io.github.shared.data.component.ProjectileAttackComponent;
import io.github.shared.data.component.ProprietyComponent;
import io.github.shared.data.component.TargetComponent;
import io.github.shared.shared_engine.Utility;

/**
 * Projectile Attack System — spawns a projectile instead of applying direct damage.
 *
 * Strict sequence:
 *  1) Validate PRIMARY target with isProjectileDistanceValid(...).
 *  2) If PRIMARY not valid/in range, validate SECONDARY target.
 *  3) If NOT forced and still no candidate, search "enemies around" and pick the nearest
 *     that passes isProjectileDistanceValid(...).
 *  4) Cooldown handling:
 *     - If a valid enemy was found AND cooldown is ready: call createProjectile(...), then
 *       reset cooldown to (base + animation/focus extra).
 *     - Else: if no valid entity found, raise currentCooldown to at least animation/focus extra;
 *       otherwise tick cooldown down normally.
 *
 * Implementation notes:
 *  - This system does NOT apply damage: the projectile creation is delegated to createProjectile(...),
 *    which you should implement to use your server’s creation pipeline (e.g., CreateInstruction).
 *  - The distance validity check is abstracted behind isProjectileDistanceValid(...): replace with
 *    your real logic (LoS, raycasts, ballistic arc, obstacles, etc.).
 */
@Wire
public class ProjectileAttackSystem extends IteratingSystem {

    // Artemis component mappers
    private ComponentMapper<ProjectileAttackComponent> mProjAttack;
    private ComponentMapper<PositionComponent> mPos;
    private ComponentMapper<ProprietyComponent> mProp;
    private ComponentMapper<TargetComponent> mTarget;
    private ComponentMapper<NetComponent> mNet;

    // Server reference to integrate projectile creation with your instruction system
    private final ServerGame server;

    /**
     * Requires ServerGame to route projectile creation to your server pipeline.
     */
    public ProjectileAttackSystem(ServerGame server) {
        super(Aspect.all(ProjectileAttackComponent.class, PositionComponent.class, ProprietyComponent.class, TargetComponent.class,NetComponent.class).exclude(FreezeComponent.class, OnCreationComponent.class));
        this.server = server;
    }

    @Override
    protected void process(int e) {
        final float dt = world.getDelta(); // per-frame delta for cooldown ticking

        // Fetch attacker components
        ProjectileAttackComponent attack = mProjAttack.get(e); // range, cooldown, projectileType
        PositionComponent pos = mPos.get(e);// attacker position
        ProprietyComponent meP = mProp.get(e);// attacker team (ally/enemy checks)
        TargetComponent tgt = mTarget.get(e);// primary/secondary targets, force flag
        NetComponent net = mNet.get(e);// netId


        // Helper: squared range to avoid sqrt while comparing distances
        final float range2 = attack.range * attack.range;

        //(1) PRIMARY target: check in-range (3D distance <= range)
        int candidateId = -1;
        boolean inRange = false;

        if (tgt != null && tgt.hasTarget()) {
            PositionComponent tPos = mPos.get(tgt.targetId);
            if (tPos != null) {
                float dx = tPos.x - pos.x, dy = tPos.y - pos.y, dz = tPos.z - pos.z;
                float dist2 = dx*dx + dy*dy + dz*dz;
                if (dist2 <= range2) {
                    candidateId = tgt.targetId;
                    inRange = true;
                }
            }
        }



        // ---- (2) SECONDARY target if primary wasn't in range ----
        if (!inRange && tgt != null && tgt.hasNextTarget()) {
            PositionComponent tPos2 = mPos.get(tgt.nextTargetId);
            if (tPos2 != null) {
                float dx = tPos2.x - pos.x, dy = tPos2.y - pos.y, dz = tPos2.z - pos.z;
                float dist2 = dx*dx + dy*dy + dz*dz;
                if (dist2 <= range2) {
                    candidateId = tgt.nextTargetId;
                    inRange = true;
                }
            }
        }


        // Forced behavior: do NOT switch to "around" if forced is true
        final boolean forced = (tgt != null && tgt.force);

        //(3) ENEMIES AROUND — nearest enemy passing the validity check (only if NOT forced)
        if (!forced && !inRange) {
            IntBag bag = world.getAspectSubscriptionManager().get(Aspect.all(PositionComponent.class, ProprietyComponent.class, LifeComponent.class)).getEntities();
            int[] ids = bag.getData();
            float bestDist2 = Float.MAX_VALUE;

            for (int i = 0, n = bag.size(); i < n; i++) {
                int other = ids[i];
                if (other == e) continue; // skip self

                // Enemy-only: teams must differ
                ProprietyComponent oP = mProp.get(other);
                if (oP == null || meP == null || oP.team == null || meP.team == null) continue;
                if (oP.team.equals(meP.team)) continue; // ignore allies

                PositionComponent oPos = mPos.get(other);
                if (oPos == null) continue;

                float dx = oPos.x - pos.x, dy = oPos.y - pos.y, dz = oPos.z - pos.z;
                float dist2 = dx*dx + dy*dy + dz*dz;

                if (dist2 <= range2 && dist2 < bestDist2) {
                    bestDist2 = dist2;
                    candidateId = other;
                    inRange = true;
                }
            }
        }


        // ---- (4) Cooldown & final decision ----
        final boolean ready = attack.isReady(); // is cooldown ready this frame?

        // Friendly-fire prevention: double-check candidate is an enemy
        boolean isEnemy = false;
        if (candidateId != -1) {
            ProprietyComponent tP = mProp.get(candidateId);
            isEnemy = (tP == null || meP == null || tP.team == null || meP.team == null || !tP.team.equals(meP.team));
        }

        // We can shoot only if we have a valid enemy candidate in range and cooldown is ready
        final boolean canShoot = (candidateId != -1 && inRange && isEnemy);

        if (ready && canShoot) {
            // === Spawn projectile (no direct damage) via placeholder ===

            // Read weapon metadata: animation/focus extra to add to cooldown
            WeaponType wt  = attack.weaponType;
            float extra    = (wt != null ? wt.getAnimationAndFocusCooldown() : 0f);

            PositionComponent tPos = mPos.get(candidateId); // target position for projectile trajectory
            if (tPos != null && meP != null) {
                // Delegate projectile creation to your server-side creation pipeline
                // Implement createProjectile(...) to use CreateInstruction or your factory
                server.addCreateInstruction(attack.projectileType,null, Utility.getNetId(),net.netId,tPos.x,tPos.y,meP.player);
            }

            // Reset cooldown to base + animation/focus extra
            attack.currentCooldown = attack.cooldown + extra;

        } else {
            // No shot this frame: enforce animation/focus minimum or tick down
            WeaponType wt = attack.weaponType;
            float extra   = (wt != null ? wt.getAnimationAndFocusCooldown() : 0f);

            boolean noEntityFound = (candidateId == -1);
            if (noEntityFound && attack.currentCooldown < extra) {
                // Raise cooldown to the animation/focus threshold (penalize shooting without targets)
                attack.currentCooldown = extra;
                // Do NOT tick down this frame.
            } else {
                // Normal cooldown ticking when threshold met or a candidate existed but couldn't be shot
                attack.updateCooldown(dt);
            }
        }
    }
}

