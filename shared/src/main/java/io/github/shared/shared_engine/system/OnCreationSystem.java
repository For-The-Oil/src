package io.github.shared.shared_engine.system;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.annotations.Wire;
import com.artemis.systems.IteratingSystem;

import io.github.shared.data.component.NetComponent;
import io.github.shared.data.component.OnCreationComponent;
import io.github.shared.data.snapshot.ComponentSnapshot;

/**
 * OnCreationSystem
 *
 * Responsibilities:
 *  - Iterate over entities that currently have an OnCreationComponent.
 *  - When OnCreationComponent expires (time <= 0), remove it and also schedule
 *    the addition of a FreezeComponent for this entity (through the SnapshotTracker).
 *
 * Design notes:
 *  - Freeze duration can be read from the entity's type metadata (EntityType.getFreeze_time()) or
 *    hard-coded if desired.
 */
@Wire
public class OnCreationSystem extends IteratingSystem {


    // Mappers injected by Artemis-ODB
    private ComponentMapper<OnCreationComponent> mOnCreation;
    private ComponentMapper<NetComponent> mNet;

    /**
     * Requires ServerGame so we can push snapshots to its SnapshotTracker.
     * The aspect targets entities that have both OnCreationComponent and NetComponent.
     */
    public OnCreationSystem() {
        super(Aspect.all(OnCreationComponent.class, NetComponent.class));
    }

    /**
     * Per-entity update step:
     *  - If OnCreationComponent.time <= 0:
     *      * remove the OnCreationComponent from the entity;
     *      * register a snapshot to add/update FreezeComponent with a desired 'freeze_time'.
     *  - Else:
     *      * decrement 'time' by delta and register a snapshot for OnCreationComponent (overwrite semantics).
     */
    @Override
    protected void process(int e) {
        // Fetch the required components for this entity
        OnCreationComponent occ = mOnCreation.get(e);// creation metadata and countdown

        // --- Update path: still in "creation" state, tick down the timer and inform clients ---
        // Reduce the remaining time, clamped to zero
        occ.time = Math.max(occ.time - world.getDelta(), 0f);

        // --- Expiration path: remove OnCreation and add a Freeze snapshot ---
        if (occ.time < 0f) {
            // Remove OnCreationComponent: entity is no longer in "creation" state
            world.getEntity(e).edit().remove(OnCreationComponent.class);
            // Nothing more to do for this entity this frame
            return;
        }
    }
}
