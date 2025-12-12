package io.github.server.game_engine.system;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.annotations.Wire;
import com.artemis.systems.IteratingSystem;

import io.github.server.data.ServerGame;
import io.github.shared.data.enums_types.EntityType;
import io.github.shared.data.component.NetComponent;
import io.github.shared.data.component.OnCreationComponent;
import io.github.shared.data.component.PositionComponent;
import io.github.shared.data.snapshot.ComponentSnapshot;
import io.github.shared.shared_engine.Utility;
import io.github.shared.shared_engine.manager.EcsManager;

/**
 * OnCreationSystem
 *
 * Responsibilities:
 *  - Iterate over entities that currently have an OnCreationComponent.
 *  - When (time > 0), decrement 'time' by world delta and register a snapshot update
 *    so clients and server-side logic stay in sync.
 *
 * Design notes:
 *  - We do NOT create/remove components by sending instructions directly within this system;
 *    instead we leverage ServerGame's SnapshotTracker to aggregate per-entity component diffs and
 *    later emit a single UpdateEntityInstruction for the frame.
 *  - Freeze duration can be read from the entity's type metadata (EntityType.getFreeze_time()) or
 *    hard-coded if desired.
 */
@Wire
public class OnCreationServerSystem extends IteratingSystem {

    // Server reference used to register component snapshots (aggregated per frame)
    private final ServerGame server;

    // Mappers injected by Artemis-ODB
    private ComponentMapper<OnCreationComponent> mOnCreation;
    private ComponentMapper<NetComponent> mNet;
    private ComponentMapper<PositionComponent> mPos;

    /**
     * Requires ServerGame so we can push snapshots to its SnapshotTracker.
     * The aspect targets entities that have both OnCreationComponent and NetComponent.
     */
    public OnCreationServerSystem(ServerGame server) {
        super(Aspect.all(OnCreationComponent.class, NetComponent.class));
        this.server = server;
    }

    /**
     * Per-entity update step:
     *      * decrement 'time' by delta and register a snapshot for OnCreationComponent (overwrite semantics).
     */
    @Override
    protected void process(int e) {

        // Fetch the required components for this entity
        OnCreationComponent occ = mOnCreation.get(e);// creation metadata and countdown
        if (occ.time < 0f)return;
        NetComponent net = mNet.get(e);// netId and type information for instruction routing

        if(occ.fromNetId != -1){
            int from = EcsManager.getIdByNetId(world,occ.fromNetId,mNet);
            if(from == -1){
                server.addDestroyInstruction(net.netId);
                return;
            }
        }

        // --- Update path: still in "creation" state, tick down the timer and inform clients ---
        // Reduce the remaining time, clamped to zero
        float newTime = Math.max(occ.time - world.getDelta(), 0f);

        if(newTime <= 0f) {
            // Prepare a snapshot for OnCreationComponent (overwrite fields)
            java.util.HashMap<String, Object> fields = new java.util.HashMap<>();
            fields.put("time", -1);// updated countdown
            fields.put("fromNetId", occ.fromNetId); // optional: keep origin for visibility/debug
            ComponentSnapshot onCreationSnap = new ComponentSnapshot("OnCreationComponent", fields);

            // Register the snapshot into the tracker for aggregation this frame
            server.getUpdateTracker().markComponentModified(world.getEntity(e), onCreationSnap);

            if (net.entityType.getType().equals(EntityType.Type.Unit)) {
                float x = 0;
                float y = 0;
                PositionComponent positionComponent = EcsManager.getPositionByNetId(world, occ.fromNetId, mNet, mPos);
                if (positionComponent != null) {
                    x = positionComponent.x;
                    y = positionComponent.y;
                }

                // Prepare a snapshot for PositionComponent (overwrite fields)
                java.util.HashMap<String, Object> fieldsPosition = new java.util.HashMap<>();
                fieldsPosition.put("x", x);
                fieldsPosition.put("y", y);
                fieldsPosition.put("z", 0f);
                fieldsPosition.put("horizontalRotation", 0f);
                fieldsPosition.put("verticalRotation", 0f);

                ComponentSnapshot onCreationSnapPosition = new ComponentSnapshot("PositionComponent", fieldsPosition);

                // Register the snapshot into the tracker for aggregation this frame
                server.getUpdateTracker().markComponentModified(world.getEntity(e), onCreationSnapPosition);
            }
        }
    }

}
