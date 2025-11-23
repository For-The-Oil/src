package io.github.server.game_engine.system;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.annotations.Wire;
import com.artemis.systems.IteratingSystem;
import com.artemis.utils.IntBag;

import io.github.server.data.ServerGame;
import io.github.shared.data.EnumsTypes.EntityType;
import io.github.shared.data.component.NetComponent;
import io.github.shared.data.component.OnCreationComponent;
import io.github.shared.data.component.PositionComponent;
import io.github.shared.data.snapshot.ComponentSnapshot;

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
        // Frame delta time (seconds) as provided by the Artemis world
        final float dt = world.getDelta();

        // Fetch the required components for this entity
        OnCreationComponent occ = mOnCreation.get(e);// creation metadata and countdown
        NetComponent net = mNet.get(e);// netId and type information for instruction routing

        // --- Expiration path: remove OnCreation and add a Freeze snapshot ---
        if (occ.time <= 0f)return;
        if(net.entityType.getType().equals(EntityType.Type.Unit)){
            float x = 0;
            float y = 0;
            PositionComponent positionComponent = getPositionByNetId(occ.fromNetId);
            if(positionComponent!=null){
                x = positionComponent.x;
                y = positionComponent.y;
            }

            // Prepare a snapshot for PositionComponent (overwrite fields)
            java.util.HashMap<String, Object> fields = new java.util.HashMap<>();
            fields.put("x", x);
            fields.put("y", y);
            fields.put("z",0f);
            fields.put("horizontalRotation",0f );
            fields.put("verticalRotation",0f );

            ComponentSnapshot onCreationSnap = new ComponentSnapshot("PositionComponent", fields);

            // Register the snapshot into the tracker for aggregation this frame
            server.getUpdateTracker().markComponentModified(world.getEntity(e), onCreationSnap);
        }

        // --- Update path: still in "creation" state, tick down the timer and inform clients ---
        // Reduce the remaining time, clamped to zero
        float newTime = Math.max(occ.time - dt, 0f);

        // Prepare a snapshot for OnCreationComponent (overwrite fields)
        java.util.HashMap<String, Object> fields = new java.util.HashMap<>();
        fields.put("time", newTime);// updated countdown
        fields.put("fromNetId", occ.fromNetId); // optional: keep origin for visibility/debug

        ComponentSnapshot onCreationSnap = new ComponentSnapshot("OnCreationComponent", fields);

        // Register the snapshot into the tracker for aggregation this frame
        server.getUpdateTracker().markComponentModified(world.getEntity(e), onCreationSnap);
    }

    protected PositionComponent getPositionByNetId(int netId) {
        if (world == null || netId < 0) return null;

        // Récupère tous les entités qui possèdent NetComponent
        IntBag entities = world.getAspectSubscriptionManager().get(Aspect.all(NetComponent.class,PositionComponent.class)).getEntities();

        int[] ids = entities.getData();
        for (int i = 0, size = entities.size(); i < size; i++) {
            int eId = ids[i];
            NetComponent net = mNet.get(eId);
            if (net != null && net.netId == netId && net.isValid()) { // netId + validité
                return mPos.get(eId);
            }
        }
        return null; // pas trouvé
    }

}
