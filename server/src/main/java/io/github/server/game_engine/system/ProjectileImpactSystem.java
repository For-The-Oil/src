package io.github.server.game_engine.system;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.annotations.Wire;
import com.artemis.systems.IteratingSystem;
import com.artemis.utils.IntBag;

import io.github.server.data.ServerGame;
import io.github.shared.data.component.LifeComponent;
import io.github.shared.data.component.MoveComponent;
import io.github.shared.data.component.NetComponent;
import io.github.shared.data.component.PositionComponent;
import io.github.shared.data.component.ProjectileComponent;
import io.github.shared.data.component.ProprietyComponent;
import io.github.shared.data.gameobject.DamageEntry;
import io.github.shared.data.snapshot.ComponentSnapshot;

/**
 * ProjectileImpactSystem
 *
 * Responsibilities:
 *  - Detect when a projectile has reached its destination (MoveComponent.destinationX/Y).
 *  - Apply AOE damage to all enemy entities within ProjectileComponent.aoe radius.
 *  - Register damage via SnapshotTracker (aggregated later into UpdateEntityInstruction).
 *  - Destroy the projectile entity using server.addDestroyInstruction(net.netId).
 *
 * Notes:
 *  - Damage is applied through snapshots, not direct component mutation.
 *  - This system assumes projectile movement is handled elsewhere and that
 *    destinationX/Y represent the final impact point.
 */
@Wire
public class ProjectileImpactSystem extends IteratingSystem {

    private ComponentMapper<ProjectileComponent> mProjectile;
    private ComponentMapper<MoveComponent> mMove;
    private ComponentMapper<PositionComponent> mPos;
    private ComponentMapper<ProprietyComponent> mProp;
    private ComponentMapper<NetComponent> mNet;

    private final ServerGame server;

    public ProjectileImpactSystem(ServerGame server) {
        super(Aspect.all(ProjectileComponent.class, MoveComponent.class, PositionComponent.class, NetComponent.class));
        this.server = server;
    }

    @Override
    protected void process(int e) {
        ProjectileComponent proj = mProjectile.get(e);
        MoveComponent move = mMove.get(e);
        PositionComponent pos = mPos.get(e);
        NetComponent net = mNet.get(e);

        // 1) Check if projectile reached its destination
        // Simple condition: current position matches destination (or very close).
        if (!hasReachedDestination(pos, move)) {
            return; // still traveling, do nothing
        }

        // 2) Apply AOE damage to all enemies within proj.aoe radius
        float aoeRadius = proj.aoe;
        float aoeRadius2 = aoeRadius * aoeRadius;

        // Scan all entities with Position + Propriety + LifeComponent.class (potential targets)
        IntBag bag = world.getAspectSubscriptionManager().get(Aspect.all(PositionComponent.class, ProprietyComponent.class, LifeComponent.class)).getEntities();
        int[] ids = bag.getData();

        for (int i = 0, n = bag.size(); i < n; i++) {
            int other = ids[i];
            if (other == e) continue; // skip the projectile itself

            ProprietyComponent oProp = mProp.get(other);
            if (oProp == null) continue;

            PositionComponent oPos = mPos.get(other);
            if (oPos == null) continue;

            // Compute squared distance from impact point
            float dx = oPos.x - move.destinationX;
            float dy = oPos.y - move.destinationY;
            float dz = oPos.z - pos.z; // use projectile's z for simplicity
            float dist2 = dx * dx + dy * dy + dz * dz;

            if (dist2 <= aoeRadius2) {
                // Check enemy (different team)
                ProprietyComponent projOwner = mProp.get(e); // projectile's team info
                boolean isEnemy = (projOwner == null || projOwner.team == null ||
                    oProp.team == null || !oProp.team.equals(projOwner.team));
                if (!isEnemy) continue;

                // Register damage snapshot for this entity
                java.util.ArrayList<Object> entries = new java.util.ArrayList<>();
                entries.add(new DamageEntry(net.netId, proj.damage, 0f)); // no armorPen for projectile by default

                java.util.HashMap<String, Object> fields = new java.util.HashMap<>();
                fields.put("entries", entries);

                ComponentSnapshot damageSnap = new ComponentSnapshot("DamageComponent", fields);
                server.getUpdateTracker().markComponentModified(world.getEntity(other), damageSnap);
            }
        }

        // 3) Destroy the projectile entity
        if (net != null && net.isValid()) {
            server.addDestroyInstruction(net.netId); // enqueue destruction for this projectile
        }
    }

    /**
     * Checks if the projectile has reached its destination.
     * Replace with your own tolerance or logic (e.g., epsilon for floating-point).
     */
    private boolean hasReachedDestination(PositionComponent pos, MoveComponent move) {
        float dx = pos.x - move.destinationX;
        float dy = pos.y - move.destinationY;
        return (Math.abs(dx) < 0.01f && Math.abs(dy) < 0.01f); // simple threshold
    }
}

