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
import io.github.shared.data.component.MoveComponent;
import io.github.shared.data.enums_types.EntityType;
import io.github.shared.data.component.FreezeComponent;
import io.github.shared.data.component.LifeComponent;
import io.github.shared.data.component.NetComponent;
import io.github.shared.data.component.OnCreationComponent;
import io.github.shared.data.component.PositionComponent;
import io.github.shared.data.component.ProjectileAttackComponent;
import io.github.shared.data.component.ProprietyComponent;
import io.github.shared.data.component.TargetComponent;
import io.github.shared.data.snapshot.ComponentSnapshot;
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
    private ComponentMapper<BuildingMapPositionComponent> bPos;
    private ComponentMapper<ProprietyComponent> mProp;
    private ComponentMapper<TargetComponent> mTarget;
    private ComponentMapper<NetComponent> mNet;
    private ComponentMapper<MoveComponent> mMove;

    // Server reference to integrate projectile creation with your instruction system
    private final ServerGame server;
    final float EPS = 1e-4f;

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
        MoveComponent move = mMove.get(e);
        NetComponent net = mNet.get(e);// netId
        float time = attack.currentCooldown-world.getDelta();
        float tmp = attack.horizontalRotation;


        // Helper: squared range to avoid sqrt while comparing distances
        final float range2 = attack.range * attack.range;

        //(1) PRIMARY target: check in-range (3D distance <= range)
        int candidateId = -1;
        boolean inRange = false;
        boolean isTypeBuilding = false;
        boolean sendCurrentCooldown = false;
        float BuildingPosx = -1;
        float BuildingPosy = -1;

        if (tgt != null && tgt.hasTarget()) {
            int te = Utility.getIdByNetId(world,tgt.targetNetId, mNet);
            PositionComponent tPos = mPos.get(te);
            NetComponent tnet = mNet.get(te);
            if(tnet != null && tnet.entityType.getType().equals(EntityType.Type.Building)){
                BuildingMapPositionComponent bp = bPos.get(te);
                ArrayList<Float> arrayList = Utility.isAttackValidForBuilding(pos,tPos,attack.range,tnet.entityType.getShapeType(),bp.direction);
                if(!arrayList.isEmpty()) {
                    BuildingPosx = arrayList.get(0);
                    BuildingPosy = arrayList.get(1);
                    candidateId = tgt.targetId;
                    inRange = true;
                    isTypeBuilding = true;
                }
            }
            else if (tPos != null) {
                float dx = tPos.x - pos.x, dy = tPos.y - pos.y, dz = tPos.z - pos.z;
                float dist2 = dx*dx + dy*dy + dz*dz;
                if (dist2 <= range2) {
                    candidateId = tgt.targetId;
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



        // ---- (2) SECONDARY target if primary wasn't in range ----
        if (!inRange && tgt != null && tgt.hasNextTarget()) {
            int te2 = Utility.getIdByNetId(world,tgt.targetNetId, mNet);
            PositionComponent tPos2 = mPos.get(te2);
            NetComponent tnet2 = mNet.get(te2);
            if(tnet2 != null && tnet2.entityType.getType().equals(EntityType.Type.Building)){
                BuildingMapPositionComponent bp = bPos.get(te2);
                ArrayList<Float> arrayList = Utility.isAttackValidForBuilding(pos,tPos2,attack.range,tnet2.entityType.getShapeType(),bp.direction);
                if(!arrayList.isEmpty()) {
                    BuildingPosx = arrayList.get(0);
                    BuildingPosy = arrayList.get(1);
                    candidateId = tgt.nextTargetId;
                    inRange = true;
                    isTypeBuilding = true;
                }
            }
            else if (tPos2 != null) {
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
                NetComponent onet = mNet.get(other);
                if(onet != null && onet.entityType.getType().equals(EntityType.Type.Building)){
                    PositionComponent oPos = mPos.get(other);
                    BuildingMapPositionComponent bp = bPos.get(other);
                    ArrayList<Float> arrayList = Utility.isAttackValidForBuilding(pos,oPos,attack.range,onet.entityType.getShapeType(),bp.direction);
                    if(!arrayList.isEmpty()) {
                        float dx = arrayList.get(0) - pos.x;
                        float dy = arrayList.get(1) - pos.y;
                        float dz = oPos.z - pos.z;
                        float dist2 = dx * dx + dy * dy + dz * dz;
                        bestDist2 = dist2;
                        candidateId = other;
                        inRange = true;
                        isTypeBuilding = true;
                    }
                }
                else {

                    PositionComponent oPos = mPos.get(other);
                    if (oPos == null) continue;

                    float dx = oPos.x - pos.x, dy = oPos.y - pos.y, dz = oPos.z - pos.z;
                    float dist2 = dx * dx + dy * dy + dz * dz;

                    if (dist2 <= range2 && dist2 < bestDist2) {
                        bestDist2 = dist2;
                        candidateId = other;
                        inRange = true;
                    }
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
        if(canShoot) {
            float tPosx = -1;
            float tPosy = -1;
            PositionComponent tPos = mPos.get(candidateId);
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
                float target = (float) Math.atan2(dy, dx);
                float delta = (float) Math.atan2(Math.sin(target - pos.horizontalRotation), Math.cos(target - pos.horizontalRotation));
                float alpha = attack.weaponType.getTurn_speed() * world.getDelta(); // ex. 0..1/frame
                alpha = Math.max(0f, Math.min(1f, alpha));
                tmp = pos.horizontalRotation + delta * alpha;
                tmp = (float) Math.atan2(Math.sin(tmp), Math.cos(tmp));

                // Seuil de changement pour éviter des updates inutiles
                boolean changed = Math.abs(tmp - pos.horizontalRotation) > EPS;

                if (changed) {
                    ComponentSnapshot previousSnapshot = server.getUpdateTracker().getPreviousSnapshot(world.getEntity(e), "ProjectileAttackComponent");
                    if (previousSnapshot != null) {
                        previousSnapshot.getFields().put("horizontalRotation", tmp);
                    } else {
                        HashMap<String, Object> fields = new HashMap<>();
                        fields.put("weaponType", attack.weaponType);
                        fields.put("cooldown", attack.cooldown);
                        fields.put("currentCooldown", attack.currentCooldown);
                        fields.put("range", attack.range);
                        fields.put("EntityType", attack.projectileType);
                        fields.put("horizontalRotation", tmp);
                        fields.put("verticalRotation", attack.verticalRotation);
                        ComponentSnapshot atkComp = new ComponentSnapshot("ProjectileAttackComponent", fields);
                        server.getUpdateTracker().markComponentModified(world.getEntity(e), atkComp);
                    }
                    if (!attack.weaponType.isHitAndMove()) {
                        ComponentSnapshot prevPos = server.getUpdateTracker()
                            .getPreviousSnapshot(world.getEntity(e), "PositionComponent");
                        if (prevPos != null) {
                            prevPos.getFields().put("horizontalRotation", Math.atan2(dy, dx));
                        } else {
                            HashMap<String, Object> fields2 = new HashMap<>();
                            fields2.put("x", pos.x);
                            fields2.put("y", pos.y);
                            fields2.put("z", pos.z);
                            fields2.put("horizontalRotation", Math.atan2(dy, dx));
                            fields2.put("verticalRotation", pos.verticalRotation);
                            ComponentSnapshot posComp = new ComponentSnapshot("PositionComponent", fields2);
                            server.getUpdateTracker().markComponentModified(world.getEntity(e), posComp);
                        }
                    }
                }else pos.horizontalRotation = tmp;
            }
        }
        if (ready && canShoot) {
            PositionComponent tPos = mPos.get(candidateId); // target position for projectile trajectory
            if (tPos != null && meP != null) {
                // Delegate projectile creation to your server-side creation pipeline
                // Implement createProjectile(...) to use CreateInstruction or your factory
                server.addCreateInstruction(attack.projectileType,null, Utility.getNetId(),net.netId,tPos.x,tPos.y,meP.player);
            }

            // Reset cooldown to base
            time = attack.weaponType.getCooldown();
            sendCurrentCooldown = true;

        } else if(!canShoot) {
            // No shot this frame: enforce animation/focus minimum or tick down
            float extra = attack.weaponType.getAnimationAndFocusCooldown();
            if (attack.currentCooldown < extra) {
                // Raise cooldown to the animation/focus threshold (penalize shooting without targets)
                time = extra;
                sendCurrentCooldown = true;
            }
        }

        if(sendCurrentCooldown ||
            attack.currentCooldown > attack.weaponType.getAnimationCooldown() && time < attack.weaponType.getAnimationCooldown() ||
            attack.currentCooldown > attack.weaponType.getAnimationAndFocusCooldown() && time < attack.weaponType.getAnimationAndFocusCooldown() ||
            time <= 0f)
        {
            ComponentSnapshot previousSnapshot = server.getUpdateTracker().getPreviousSnapshot(world.getEntity(e),"ProjectileAttackComponent");
            if(previousSnapshot != null){
                previousSnapshot.getFields().put("currentCooldown",time);
            }
            else {
                java.util.HashMap<String, Object> fields = new java.util.HashMap<>();
                fields.put("weaponType", attack.weaponType);
                fields.put("cooldown", attack.cooldown);
                fields.put("currentCooldown", time);
                fields.put("range", attack.range);
                fields.put("projectileType", attack.projectileType);
                fields.put("horizontalRotation", tmp);
                fields.put("verticalRotation", attack.verticalRotation);
                ComponentSnapshot damageSnap = new ComponentSnapshot("ProjectileAttackComponent", fields);
                server.getUpdateTracker().markComponentModified(world.getEntity(e), damageSnap);
            }
        }
    }
}

