package io.github.shared.shared_engine.system;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.annotations.Wire;
import com.artemis.systems.IteratingSystem;
import io.github.shared.data.component.PositionComponent;
import io.github.shared.data.component.VelocityComponent;

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

    /** Maximum angular speed in radians per second (360°/s by default). */
    private static final float TURN_SPEED = (float) Math.toRadians(360f);

    /** Small epsilon to detect near-zero speed and avoid division issues. */
    private static final float EPS = 1e-5f;

    /** Injected by Artemis: access to position and velocity components. */
    private ComponentMapper<PositionComponent> mPos;
    private ComponentMapper<VelocityComponent> mVel;

    /**
     * Constructs the system for entities that have both Position and Velocity.
     */
    public VectorApplicationSystem() {
        super(Aspect.all(PositionComponent.class, VelocityComponent.class));
    }

    /**
     * Per-entity update:
     * 1) Integrate position using velocity.
     * 2) Compute target yaw/pitch from velocity direction.
     * 3) Rotate current yaw/pitch toward targets with a capped step.
     *
     * @param entityId Artemis entity ID
     */
    @Override
    protected void process(int entityId) {
        // Fetch components
        PositionComponent pos = mPos.get(entityId);
        VelocityComponent vel = mVel.get(entityId);

        // Delta time from the ECS world (seconds)
        float dt = world.getDelta();

        // --- (1) Position integration in 3D) ---
        // x += vx * dt; y += vy * dt; z += vz * dt
        pos.translate(vel.vx * dt, vel.vy * dt, vel.vz * dt);

        // --- (2) Determine target yaw/pitch from velocity direction ---
        // Use squared speed for an efficient "moving" check
        float speedSq = vel.vx * vel.vx + vel.vy * vel.vy + vel.vz * vel.vz;
        if (speedSq > EPS) {
            // Yaw from XY projection
            float targetYaw = (float) Math.atan2(vel.vy, vel.vx);

            // Pitch from vertical vs horizontal speed (avoid division by zero with EPS)
            float horiz = (float) Math.sqrt(vel.vx * vel.vx + vel.vy * vel.vy);
            float targetPitch = (float) Math.atan2(vel.vz, Math.max(horiz, EPS));

            // --- (3) Smoothly rotate toward targets ---
            // Limit per-frame rotation to TURN_SPEED * dt
            float step = TURN_SPEED * dt;
            pos.horizontalRotation = rotateTowards(pos.horizontalRotation, targetYaw, step);
            pos.verticalRotation   = rotateTowards(pos.verticalRotation,   targetPitch, step);
        }
        // If not moving, keep current rotations as-is.
    }

    /**
     * Moves the angle 'current' toward 'target' by at most 'maxStep' radians,
     * accounting for wrap-around on [-π, π].
     *
     * @param current current angle (radians)
     * @param target  target angle (radians)
     * @param maxStep maximum angular change allowed this frame (radians)
     * @return new angle after stepping toward target
     */
    private static float rotateTowards(float current, float target, float maxStep) {
        float diff = wrapPi(target - current);
        float step = clamp(diff, -maxStep, maxStep);
        return wrapPi(current + step);
    }

    /**
     * Wraps an angle to the [-π, π] interval.
     *
     * @param a angle in radians
     * @return wrapped angle in radians
     */
    private static float wrapPi(float a) {
        while (a <= -Math.PI) a += (float) (2 * Math.PI);
        while (a >  Math.PI)  a -= (float) (2 * Math.PI);
        return a;
    }

    /**
     * Clamps a value within [lo, hi].
     *
     * @param v  value to clamp
     * @param lo lower bound
     * @param hi upper bound
     * @return clamped value
     */
    private static float clamp(float v, float lo, float hi) {
        return Math.max(lo, Math.min(hi, v));
    }
}

