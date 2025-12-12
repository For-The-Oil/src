
package io.github.shared.shared_engine.system;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.annotations.Wire;
import com.artemis.systems.IteratingSystem;


import io.github.shared.data.component.BuildingMapPositionComponent;
import io.github.shared.data.component.FreezeComponent;
import io.github.shared.data.component.MoveComponent;
import io.github.shared.data.component.NetComponent;
import io.github.shared.data.component.PositionComponent;
import io.github.shared.data.component.ProjectileComponent;
import io.github.shared.data.component.SpeedComponent;
import io.github.shared.data.component.TargetComponent;
import io.github.shared.data.component.VelocityComponent;

/**
 * MovementSystem (Local)
 *
 * This system runs locally (client-side or shared logic) and applies movement-related updates
 * directly to components. It is responsible for:
 * - Resetting movement when the entity reaches its destination.
 * - Resetting movement when the target becomes invalid.
 *
 * Unlike the server-side system, this one does not send snapshots; it applies changes immediately.
 */
@Wire
public class MovementSystem extends IteratingSystem {

    /** Tolerance for considering an entity as having reached its destination. */
    private static final float EPSILON = 5f;

    // Component mappers for quick access to entity components
    private ComponentMapper<PositionComponent> mPos;
    private ComponentMapper<VelocityComponent> mVel;
    private ComponentMapper<MoveComponent> mMove;
    private ComponentMapper<TargetComponent> mTarget;

    /**
     * Constructor: Defines the aspect of entities processed by this system.
     * Entities must have Position, Velocity, Speed, and Net components.
     * Excludes frozen entities, buildings, and projectiles.
     */
    public MovementSystem() {
        super(Aspect.all(PositionComponent.class, VelocityComponent.class, SpeedComponent.class, NetComponent.class).exclude(FreezeComponent.class, BuildingMapPositionComponent.class, ProjectileComponent.class));
    }

    /**
     * Processes each entity that matches the aspect.
     * Logic:
     * - If MoveComponent is missing, skip.
     * - If movement is target-related but the target is invalid, reset MoveComponent.
     * - If movement is destination-based and the entity has reached its destination, reset MoveComponent.
     *
     * @param e The entity ID being processed.
     */
    @Override
    protected void process(int e) {
        PositionComponent pos = mPos.get(e);
        VelocityComponent vel = mVel.get(e);
        MoveComponent move = mMove.get(e);
        TargetComponent tgt = mTarget.get(e);

        // Skip if essential components are missing
        if (pos == null || vel == null || move == null) return;

        // Case 1: Movement is target-related but target is missing or invalid
        if (move.targetRelated && (tgt == null || !tgt.hasTarget())) {
            MoveComponent moveComp = mMove.get(e);
            if (moveComp != null) moveComp.reset();
            return;
        }

        // Case 2: Movement is destination-based (not target-related)
        if (!move.targetRelated) {
            float destX = move.destinationX;
            float destY = move.destinationY;

            // Ignore invalid destination coordinates
            if (destX < 0 || destY < 0) return;

            // If entity is close enough to destination, reset movement
            if (reached(pos.x, pos.y, destX, destY, EPSILON)) {
                MoveComponent moveComp = mMove.get(e);
                if (moveComp != null) {
                    moveComp.reset();
                    if (move.force) {
                        moveComp.force = true;
                    }
                }
            }
        }
    }

    /**
     * Checks if the entity has reached the destination within a given tolerance.
     *
     * @param x    Current X position
     * @param y    Current Y position
     * @param tx   Target X position
     * @param ty   Target Y position
     * @param eps  Tolerance (distance threshold)
     * @return true if the entity is within the tolerance of the destination
     */
    private boolean reached(float x, float y, float tx, float ty, float eps) {
        float dx = tx - x, dy = ty - y;
        return (dx * dx + dy * dy) <= eps * eps;
    }
}
