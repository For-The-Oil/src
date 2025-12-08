
package io.github.core.game_engine;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Vector3;

/**
 * Global camera controller for render-time zoom/rotation/reset,
 * designed to coexist with a finger-anchored gesture controller.
 * <p>
 * During a gesture (drag/pinch), an input lock can be enabled to prevent
 * concurrent camera changes. Outside gestures, zoom/rotation deltas are applied
 * around the current {@code lookTarget}.
 *
 * <h3>Recommended flow</h3>
 * <ol>
 *   <li>The gesture controller calls {@link #setInputLock(boolean)} with {@code true}
 *       at gesture start, then with {@code false} at gesture end.</li>
 *   <li>At gesture end, the gesture controller calls {@link #setLookTarget(Vector3)}
 *       to synchronize the look point.</li>
 *   <li>In the render loop, always call {@link #applyToCamera(Camera)} to apply deltas.</li>
 * </ol>
 *
 * <h3>Parameters</h3>
 * <ul>
 *   <li>{@link #zoomDelta}: positive moves forward along the camera's direction,
 *       negative moves backward (world units).</li>
 *   <li>{@link #rotDelta}: rotation angle in degrees around Y and {@code lookTarget}.</li>
 *   <li>{@link #resetRequested}: when true, camera is reset to default values.</li>
 * </ul>
 *
 * <h3>Thread-safety</h3>
 * Not thread-safe; call from the libGDX render thread.
 *
 * @author Saïd & Copilot
 */
public class CameraController {
    private static final CameraController INSTANCE = new CameraController();

    /** Zoom delta to be applied on next {@link #applyToCamera(Camera)}. */
    public float zoomDelta = 0f;

    /** Rotation delta (degrees around Y) to be applied on next {@link #applyToCamera(Camera)}. */
    public float rotDelta = 0f;

    /** Reset request (consumed on next application). */
    public boolean resetRequested = false;

    // Defaults
    private final Vector3 defaultPos = new Vector3(1500f, 2000f, 800f);
    private final Vector3 defaultTarget = new Vector3(1500f, 0f, 800f);

    // Current state
    private final Vector3 lookTarget = new Vector3(defaultTarget);
    private boolean inputLock = false;

    private CameraController(){}

    /** @return singleton instance of the camera controller. */
    public static CameraController get(){ return INSTANCE; }

    /**
     * Sets the world look point used by future operations
     * (rotation around Y, reaffirm look after zoom).
     *
     * @param t new look target (copied internally)
     */
    public void setLookTarget(Vector3 t){ this.lookTarget.set(t); }

    /**
     * Enables/disables the input lock. When enabled, {@link #applyToCamera(Camera)}
     * does not change the camera (used during gestures).
     *
     * @param lock {@code true} to freeze external changes, {@code false} to allow them
     */
    public void setInputLock(boolean lock){ this.inputLock = lock; }

    /**
     * Applies pending requests (reset, zoom, rotation) to the camera.
     * <p>
     * Application order:
     * <ol>
     *   <li>If {@link #resetRequested}: set default position and look target.</li>
     *   <li>If {@link #zoomDelta} ≠ 0: translate along the current direction.</li>
     *   <li>If {@link #rotDelta} ≠ 0: rotate around {@code lookTarget} and the Y axis.</li>
     * </ol>
     * If the input lock is enabled, the method returns immediately without applying changes.
     *
     * @param cam camera to modify
     */
    public void applyToCamera(Camera cam) {
        if (inputLock) return;

        if (resetRequested) {
            cam.position.set(defaultPos);
            cam.lookAt(defaultTarget);
            lookTarget.set(defaultTarget);
            cam.update();
            resetRequested = false;
        }

        if (zoomDelta != 0f) {
            // Zoom along the current view direction
            Vector3 forward = new Vector3(cam.direction).nor();
            cam.position.add(forward.scl(zoomDelta));
            zoomDelta = 0f;
            cam.lookAt(lookTarget);
            cam.update();
        }

        if (rotDelta != 0f) {
            // Rotate around current lookTarget and Y axis
            cam.rotateAround(lookTarget, Vector3.Y, rotDelta);
            rotDelta = 0f;
            cam.lookAt(lookTarget);
            cam.update();
        }
    }
}
