package io.github.server.game_engine.system;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.annotations.Wire;
import com.artemis.systems.IteratingSystem;
import java.util.HashMap;
import io.github.server.data.ServerGame;
import io.github.shared.data.component.*;
import io.github.shared.data.snapshot.ComponentSnapshot;

/**
 * ProjectileMovementSystem
 *
 * This system handles the movement of projectile entities toward a fixed destination (X, Y).
 * It also computes a parabolic trajectory for the Z-axis to simulate an arc.
 *
 * Responsibilities:
 * - Move the projectile toward its destination using normalized direction.
 * - Apply delta time for frame-rate independent movement.
 * - Calculate Z position based on progress for a smooth parabolic curve.
 * - Send velocity updates via snapshots (no direct component modification).
 */
@Wire
public class ProjectileMovementSystem extends IteratingSystem {

    private final ServerGame server;

    // Component mappers for quick access
    private ComponentMapper<PositionComponent> mPos;
    private ComponentMapper<MoveComponent> mMove;
    private ComponentMapper<ProjectileComponent> mProjectile;
    private ComponentMapper<NetComponent> mNet;

    /**
     * Constructor: Processes entities that have Position, Move, Projectile, and Net components.
     */
    public ProjectileMovementSystem(ServerGame server) {
        super(Aspect.all(PositionComponent.class, MoveComponent.class, ProjectileComponent.class, NetComponent.class));
        this.server = server;
    }

    /**
     * Main logic for projectile movement:
     * - Compute direction toward destination.
     * - Normalize direction vector.
     * - Calculate horizontal velocity (X, Y) using base speed and delta time.
     * - Compute Z using a parabolic formula based on progress.
     * - Send velocity update via snapshot.
     *
     * @param e Entity ID
     */
    @Override
    protected void process(int e) {
        PositionComponent pos = mPos.get(e);
        MoveComponent move = mMove.get(e);
        ProjectileComponent proj = mProjectile.get(e);
        NetComponent net = mNet.get(e);

        // Validate required components
        if (pos == null || move == null || proj == null) return;

        float destX = move.destinationX;
        float destY = move.destinationY;

        // Compute direction toward destination
        float dx = destX - pos.x;
        float dy = destY - pos.y;
        float len = (float) Math.sqrt(dx * dx + dy * dy);

        // If already at destination, stop processing
        if (len == 0f) return;

        // Normalize direction vector
        dx /= len;
        dy /= len;

        // Apply delta for frame-rate independence
        float delta = world.getDelta();

        // Base speed from entity type
        float baseSpeed = net.entityType.getProjectileSpeed();

        // Horizontal velocity (X, Y)
        float vx = dx * baseSpeed * delta;
        float vy = dy * baseSpeed * delta;

        // Compute parabolic Z based on progress
        float totalDist = (float) Math.sqrt((proj.fromX - destX) * (proj.fromX - destX) +
            (proj.fromY - destY) * (proj.fromY - destY));
        float traveledDist = (float) Math.sqrt((pos.x - proj.fromX) * (pos.x - proj.fromX) +
            (pos.y - proj.fromY) * (pos.y - proj.fromY));
        float progress = Math.min(1f, traveledDist / totalDist);

        // Parabolic formula: maxHeight * (1 - (2p - 1)^2)
        float vz = proj.maxHeight * (1 - (2 * progress - 1) * (2 * progress - 1));

        // Send velocity update via snapshot
        HashMap<String, Object> fields = new HashMap<>();
        fields.put("vx", vx);
        fields.put("vy", vy);
        fields.put("vz", vz);
        ComponentSnapshot snap = new ComponentSnapshot("VelocityComponent", fields);
        server.getUpdateTracker().markComponentModified(world.getEntity(e), snap);
    }
}
