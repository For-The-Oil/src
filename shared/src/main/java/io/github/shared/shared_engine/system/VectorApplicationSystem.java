package io.github.shared.shared_engine.system;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.annotations.Wire;
import com.artemis.systems.IteratingSystem;
import io.github.shared.data.component.PositionComponent;
import io.github.shared.data.component.VelocityComponent;
import io.github.shared.shared_engine.Utility;

/**
 * MovementSystem
 *
 * Responsibilities:
 * - Integrates 3D velocity into an entity's position each frame.
 * - Smoothly rotates yaw (horizontalRotation) and pitch (verticalRotation)
 *   toward the current velocity direction using a capped angular speed.
 *
 * Rotation model:
 * - Yaw    (horizontalRotation): derived from XY plane direction -> atan2(vy, vx)
 * - Pitch  (verticalRotation):   derived from vertical vs horizontal speed -> atan2(vz, sqrt(vx^2 + vy^2))
 *
 * Notes:
 * - Angles are treated in radians, wrapped to [-π, π] to avoid discontinuities.
 * - The rotation step is limited by TURN_SPEED * dt to ensure smooth steering.
 * - If the entity is not moving (near-zero speed), rotations are left unchanged.
 */

@Wire
public class VectorApplicationSystem extends IteratingSystem {

    private static final float TURN_SPEED = (float) Math.toRadians(360f);
    private static final float EPS = 1e-5f;
    private static final float EPS_PITCH = (float) Math.toRadians(0.1f);

    private ComponentMapper<PositionComponent> mPos;
    private ComponentMapper<VelocityComponent> mVel;

    public VectorApplicationSystem() {
        super(Aspect.all(PositionComponent.class, VelocityComponent.class));
    }


    @Override
    protected void process(int entityId) {
        PositionComponent pos = mPos.get(entityId);
        VelocityComponent vel = mVel.get(entityId);

        float dt = world.getDelta()*20;

        // (1) Intégration position
        pos.translate(vel.vx * dt, vel.vy * dt, vel.vz * dt);

        // (2) Calcul des angles cibles
        float speedSq = vel.vx * vel.vx + vel.vy * vel.vy + vel.vz * vel.vz;
        if (speedSq > EPS) {
            float raw = (float) Math.atan2(-vel.vy, vel.vx);
            float facingOffset = (float) -Math.PI/2;
            pos.horizontalRotation = Utility.normAngle(raw + facingOffset);
        }
    }
}

