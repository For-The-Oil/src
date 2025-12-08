
package io.github.android.utils;

import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Plane;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.Ray;

import io.github.core.game_engine.CameraController;

/**
 * Camera gestures anchored on the 2D map plane (e.g., y=0).
 * <ul>
 *   <li><b>One-finger PAN</b>: compute the ground projection of the finger (ray ∩ plane),
 *       and translate camera + look target so that the anchored ground point stays under the finger.</li>
 *   <li><b>Two-finger PINCH-ZOOM</b>: multiplicative (ratio-based) zoom along the view axis,
 *       clamped <b>only</b> by camera height above ground; then corrected so the anchored
 *       ground point (projection) remains under finger 1.</li>
 * </ul>
 *
 * <h3>Usage</h3>
 * <pre>{@code
 * Plane mapPlane = new Plane(new Vector3(0, 1, 0), 0f); // y = 0
 * CameraGestureController gestures = new CameraGestureController(
 *     camera,
 *     new Vector3(1500f, 0f, 800f), // initial look point
 *     mapPlane
 * );
 * Gdx.input.setInputProcessor(gestures);
 * }</pre>
 *
 * <h3>Notes</h3>
 * <ul>
 *   <li>No Y flip here: we keep InputProcessor screen coords as-is.</li>
 *   <li>Ray creation uses {@link com.badlogic.gdx.graphics.Camera#getPickRay(float, float)}.</li>
 *   <li>Zoom clamps depend only on camera height (Y above the map plane), never on the clicked object.</li>
 * </ul>
 */
public class CameraGestureController extends InputAdapter {

    private final Camera camera;
    private final Vector3 target;
    private final Plane mapPlane;

    // Optional global controller lock during gestures
    private final CameraController cc = CameraController.get();

    // Pointer state
    private int pointer1 = -1, pointer2 = -1;
    private final Vector2 prev1 = new Vector2(), prev2 = new Vector2();

    // Buffers / anchors (all on ground plane)
    private final Vector3 tmp = new Vector3();
    private final Vector3 anchorGround = new Vector3();   // ground projection at touchDown for pan & zoom correction
    private boolean hasAnchorGround = false;

    // Pinch parameters
    private final float MIN_RATIO = 0.2f, MAX_RATIO = 5f;   // per-frame clamp
    private final float PINCH_SENS = 1.0f;                  // 1 = natural, <1 = softer

    // Height-based clamps (independent of clicked object)
    private final float MIN_HEIGHT = 600f;   // min camera Y above ground
    private final float MAX_HEIGHT = 5000f;  // max camera Y above ground

    /**
     * Constructs the plane-anchored gesture controller.
     *
     * @param cam      libGDX camera (perspective or orthographic)
     * @param target   initial world look point (updated on gestures)
     * @param mapPlane map/ground plane (e.g., y=0)
     */
    public CameraGestureController(Camera cam, Vector3 target, Plane mapPlane) {
        this.camera = cam;
        this.target = target.cpy();
        this.mapPlane = mapPlane;
    }

    /** Intersects the screen ray with the map plane; returns false if ray is parallel. */
    private boolean screenToGround(int sx, int sy, Vector3 out) {
        Ray ray = camera.getPickRay(sx, sy); // keep InputProcessor coords as-is
        return Intersector.intersectRayPlane(ray, mapPlane, out);
    }

    // ------------------------------------------------------------------ //
    // Input lifecycle
    // ------------------------------------------------------------------ //

    @Override
    public boolean touchDown(int x, int y, int pointer, int button) {
        if (pointer1 == -1) {
            pointer1 = pointer;
            prev1.set(x, y);

            // Anchor on ground projection (whatever you touched)
            hasAnchorGround = screenToGround(x, y, anchorGround);

            // Lock external camera changes during gesture
            cc.setInputLock(true);
        }
        else if (pointer2 == -1) {
            pointer2 = pointer;
            prev2.set(x, y);
        }
        return true;
    }

    @Override
    public boolean touchUp(int x, int y, int pointer, int button) {
        if (pointer == pointer1) pointer1 = -1;
        else if (pointer == pointer2) pointer2 = -1;

        if (pointer1 == -1 && pointer2 == -1) {
            hasAnchorGround = false;
            cc.setInputLock(false);
            cc.setLookTarget(target); // sync look point for external controller
        }
        return true;
    }

    @Override
    public boolean touchDragged(int x, int y, int pointer) {
        if (camera == null) return false;

        // ------------------ One finger: PAN on ground projection ------------------
        if (pointer1 != -1 && pointer2 == -1) {
            Vector2 curr = (pointer == pointer1) ? new Vector2(x, y) : prev1.cpy();
            Vector3 groundNow = new Vector3();

            if (hasAnchorGround && screenToGround((int)curr.x, (int)curr.y, groundNow)) {
                // Delta purely on ground plane: anchorGround - groundNow
                tmp.set(anchorGround).sub(groundNow);
                camera.position.add(tmp);
                target.add(tmp);
                camera.lookAt(target);
                camera.update();
            }
            prev1.set(curr);
        }

        // -------- Two fingers: multiplicative PINCH (height-clamped) + ground anchor correction --------
        else if (pointer1 != -1 && pointer2 != -1) {
            Vector2 curr1 = (pointer == pointer1) ? new Vector2(x, y) : prev1.cpy();
            Vector2 curr2 = (pointer == pointer2) ? new Vector2(x, y) : prev2.cpy();

            float prevDist = Math.max(1f, prev1.dst(prev2));
            float currDist = Math.max(1f, curr1.dst(curr2));
            float ratio = prevDist / currDist;                 // >1: zoom out, <1: zoom in
            ratio = Math.max(MIN_RATIO, Math.min(MAX_RATIO, ratio));

            // Direction from target to camera (normalized)
            Vector3 dir = tmp.set(camera.position).sub(target).nor();

            // Desired distance along view axis
            float currentDistance = Math.max(1e-3f, camera.position.dst(target));
            float newDistance = (float)(currentDistance * Math.pow(ratio, PINCH_SENS));

            // --- Height clamp based ONLY on camera Y (above ground) ---
            float candidateY = target.y + dir.y * newDistance;
            float clampedY   = Math.max(MIN_HEIGHT, Math.min(MAX_HEIGHT, candidateY));

            if (Math.abs(dir.y) > 1e-6f) {
                newDistance = (clampedY - target.y) / dir.y;
            }

            // Place camera on the ray from target
            camera.position.set(target).mulAdd(dir, newDistance);

            // If view is nearly horizontal, enforce height by vertical shift (keep direction)
            if (Math.abs(dir.y) <= 1e-6f) {
                float deltaY = clampedY - camera.position.y;
                camera.position.y += deltaY;
                target.y += deltaY;
            }

            camera.lookAt(target);
            camera.update();

            prev1.set(curr1);
            prev2.set(curr2);
        }

        return true;
    }
}
