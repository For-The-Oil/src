package io.github.shared.shared_engine.system;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.annotations.Wire;
import com.artemis.systems.IteratingSystem;

import io.github.shared.data.component.FreezeComponent;
import io.github.shared.data.component.OnCreationComponent;

/**
 * FreezeSystem
 *
 * Responsibilities:
 *  - Iterate over all entities that currently have a FreezeComponent.
 *  - If freeze_time <= 0: unfreeze the entity by removing the FreezeComponent.
 *
 * Design notes:
 *  - Removal is done with entity.edit().remove(FreezeComponent.class).
 */
@Wire
public class FreezeSystem extends IteratingSystem {

    // Mapper injected by Artemis-ODB to access FreezeComponent instances quickly
    private ComponentMapper<FreezeComponent> mFreeze;

    /**
     * Constructor requires ServerGame so this system can register component snapshots
     * to be flushed later into a single UpdateEntityInstruction.
     */
    public FreezeSystem() {
        // Run over all entities that currently have a FreezeComponent
        super(Aspect.all(FreezeComponent.class).exclude(OnCreationComponent.class));
    }

    /**
     * Per-entity update:
     *  - If freeze_time <= 0: remove FreezeComponent (entity is no longer frozen).
     */
    @Override
    protected void process(int e) {
        // Fetch the required components for this entity
        FreezeComponent fc = mFreeze.get(e);// creation metadata and countdown

        // --- Expiration path: remove Freeze ---
        if (fc.freeze_time < 0f) {
            // Remove FreezeComponent: entity is no longer in "Freeze" state
            world.getEntity(e).edit().remove(FreezeComponent.class);
            // Nothing more to do for this entity this frame
            return;
        }else fc.freeze_time = Math.max(fc.freeze_time - world.getDelta(), 0f); // Reduce the remaining time, clamped to zero
    }
}

