package io.github.server.game_engine.system;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.annotations.Wire;
import com.artemis.systems.IteratingSystem;

import io.github.server.data.ServerGame;
import io.github.shared.data.component.FreezeComponent;
import io.github.shared.data.component.OnCreationComponent;
import io.github.shared.data.snapshot.ComponentSnapshot;

/**
 * FreezeSystem
 *
 * Responsibilities:
 *  - Iterate over all entities that currently have a FreezeComponent.
 *  - If: decrement freeze_time by world delta and notify the server via SnapshotTracker
 *    using a ComponentSnapshot (the tracker will later produce a single UpdateEntityInstruction
 *    for all modified entities in the frame).
 *
 * Design notes:
 *  - SnapshotTracker overwrites FreezeComponent fields (here: "freeze_time") when merging.
 *  - Keeping local state (fc.freeze_time) in sync ensures gameplay systems read updated values immediately.
 */
@Wire
public class FreezeServerSystem extends IteratingSystem {

    // Server reference used to register snapshots (aggregated updates for the frame)
    private final ServerGame server;

    // Mapper injected by Artemis-ODB to access FreezeComponent instances quickly
    private ComponentMapper<FreezeComponent> mFreeze;

    /**
     * Constructor requires ServerGame so this system can register component snapshots
     * to be flushed later into a single UpdateEntityInstruction.
     */
    public FreezeServerSystem(ServerGame server) {
        // Run over all entities that currently have a FreezeComponent
        super(Aspect.all(FreezeComponent.class).exclude(OnCreationComponent.class));
        this.server = server;
    }

    /**
     * Per-entity update:
     * decrement by delta, update local component, and register a snapshot.
     */
    @Override
    protected void process(int e) {
        // Fetch the component for this entity
        FreezeComponent fc = mFreeze.get(e);
        if (fc.freeze_time <= 0f) return;// If the freeze time has expired or is zero, unfreeze by removing the component

        // Reduce the remaining freeze time, clamped to [0, +inf)
        float newTime = Math.max(fc.freeze_time - world.getDelta(), 0f);

        if(newTime <= 0f) {
            // Prepare a snapshot describing the new FreezeComponent state.
            // SnapshotTracker merges FreezeComponent via overwrite of fields, so we only send "freeze_time".
            java.util.HashMap<String, Object> fields = new java.util.HashMap<>();
            fields.put("freeze_time", -1);
            // Type name must match the component you want the tracker to update/merge
            ComponentSnapshot snap = new ComponentSnapshot("FreezeComponent", fields);

            // Register the change in the server's SnapshotTracker:
            // it resolves netId/entityType via NetComponent and aggregates per-entity diffs.
            server.getUpdateTracker().markComponentModified(world.getEntity(e), snap);
        }
    }
}

