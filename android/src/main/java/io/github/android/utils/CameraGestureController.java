package io.github.android.utils;

import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Plane;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.math.collision.Ray;

import net.mgsx.gltf.scene3d.scene.Scene;

import java.util.ArrayList;

/**
 * Gesture controller for libGDX providing finger-anchored pan and pinch-zoom.
 * <p>
 * At every interaction, the world point under the finger is computed via a raycast.
 * If the pixel hits an object (e.g., a tank), the anchor is set on that object by
 * intersecting the ray with its world-space axis-aligned bounding box (AABB).
 * If nothing is hit, the anchor falls back to the map plane (e.g., {@code y = 0}).
 * By keeping this anchor under the finger, the touched world point stays
 * exactly under the finger while panning or zooming, regardless of the zoom level.
 *
 * <h3>Features</h3>
 * <ul>
 *   <li>Anchored pan: translate camera and look target so the anchor stays under the finger.</li>
 *   <li>Pinch-zoom: zoom along the view axis and then correct so the anchor remains under finger 1.</li>
 *   <li>Fast AABB ray-cast per scene with fallback to the map plane.</li>
 * </ul>
 *
 * <h3>Usage</h3>
 * <pre>{@code
 * Plane mapPlane = new Plane(new Vector3(0, 1, 0), 0f); // y = 0
 * CameraGestureController gestures = new CameraGestureController(
 *     camera,
 *     new Vector3(1500f, 0f, 800f),     // initial look point
 *     mapPlane,
 *     renderer::getAllScenes            // provider of pickable scenes
 * );
 * Gdx.input.setInputProcessor(gestures);
 * }</pre>
 *
 * <h3>Notes</h3>
 * <ul>
 *   <li>No Y flip is applied here to keep screen coordinates consistent with
 *       {@link com.badlogic.gdx.InputProcessor} callbacks.</li>
 *   <li>Ray creation uses {@link com.badlogic.gdx.graphics.Camera#getPickRay(float, float)}.</li>
 *   <li>Bounding boxes are obtained via
 *       {@link com.badlogic.gdx.graphics.g3d.ModelInstance#calculateBoundingBox(BoundingBox)}.
 *       Ensure transforms are up to date before rendering so AABBs are in world space.</li>
 * </ul>
 *
 * @author Saïd & Copilot
 */
public class CameraGestureController extends InputAdapter {

    /**
     * Supplies scenes to be considered for picking (AABB ray tests).
     * Typically: all map scenes plus all entity scenes.
     */
    public interface PickableSceneProvider {
        /**
         * @return iterable of pickable scenes (map + entities).
         */
        Iterable<Scene> getPickableScenes();
    }

    private final Camera camera;
    private final Vector3 target;
    private final Plane mapPlane;
    private final PickableSceneProvider provider;

    // Pointer state
    private int pointer1 = -1, pointer2 = -1;
    private final Vector2 prev1 = new Vector2(), prev2 = new Vector2();

    // Buffers
    private final Vector3 tmp = new Vector3();
    private final Vector3 anchorWorld = new Vector3();
    private boolean hasAnchor = false;

    // Zoom params
    private final float MIN_ZOOM = 500f;
    private final float MAX_ZOOM = 5000f;
    private final float ZOOM_SPEED_FACTOR = 10f;

    /**
     * Constructs the anchored gesture controller.
     *
     * @param cam       libGDX camera (perspective or orthographic)
     * @param target    initial world look point (will be updated on gestures)
     * @param mapPlane  map plane (e.g., {@code y=0})
     * @param provider  supplier of pickable scenes for AABB ray tests
     */
    public CameraGestureController(Camera cam, Vector3 target, Plane mapPlane, PickableSceneProvider provider) {
        this.camera = cam;
        this.target = target.cpy();
        this.mapPlane = mapPlane;
        this.provider = provider;
    }

    /** {@inheritDoc} */
    @Override
    public boolean touchDown(int x, int y, int pointer, int button) {
        if (pointer1 == -1) {
            pointer1 = pointer;
            prev1.set(x, y);
            hasAnchor = screenToWorldUnderPixel(x, y, anchorWorld); // anchor on object or map
        } else if (pointer2 == -1) {
            pointer2 = pointer;
            prev2.set(x, y);
        }
        return true;
    }

    /** {@inheritDoc} */
    @Override
    public boolean touchUp(int x, int y, int pointer, int button) {
        if (pointer == pointer1) pointer1 = -1;
        else if (pointer == pointer2) pointer2 = -1;
        if (pointer1 == -1 && pointer2 == -1) hasAnchor = false;
        return true;
    }

    /** {@inheritDoc} */
    @Override
    public boolean touchDragged(int x, int y, int pointer) {
        if (camera == null) return false;

        // One finger: keep anchor under pointer1
        if (pointer1 != -1 && pointer2 == -1) {
            Vector2 curr = (pointer == pointer1) ? new Vector2(x, y) : prev1.cpy();
            Vector3 worldUnderFinger = new Vector3();

            if (hasAnchor && screenToWorldUnderPixel((int)curr.x, (int)curr.y, worldUnderFinger)) {
                tmp.set(anchorWorld).sub(worldUnderFinger); // world delta
                camera.position.add(tmp);
                target.add(tmp);
                camera.lookAt(target);
                camera.update();
            }
            prev1.set(curr);
        }

        // Two fingers: pinch-zoom + keep anchor under pointer1
        else if (pointer1 != -1 && pointer2 != -1) {
            Vector2 curr1 = (pointer == pointer1) ? new Vector2(x, y) : prev1.cpy();
            Vector2 curr2 = (pointer == pointer2) ? new Vector2(x, y) : prev2.cpy();

            // Zoom (clamped camera-target distance)
            float prevDist = prev1.dst(prev2);
            float currDist = curr1.dst(curr2);
            float zoomAmount = (prevDist - currDist) * ZOOM_SPEED_FACTOR;

            Vector3 offset = tmp.set(camera.position).sub(target);
            float currentDistance = offset.len();
            float targetDistance = Math.max(MIN_ZOOM, Math.min(MAX_ZOOM, currentDistance + zoomAmount));
            offset.nor().scl(targetDistance);
            camera.position.set(target).add(offset);

            // Correct so the anchor stays under pointer1
            Vector3 worldUnderFinger1 = new Vector3();
            if (hasAnchor && screenToWorldUnderPixel((int)curr1.x, (int)curr1.y, worldUnderFinger1)) {
                tmp.set(anchorWorld).sub(worldUnderFinger1);
                camera.position.add(tmp);
                target.add(tmp);
            }

            camera.lookAt(target);
            camera.update();

            prev1.set(curr1);
            prev2.set(curr2);
        }

        return true;
    }

    // --------------------- Picking utilities ------------------------

    /**
     * Computes the world point under pixel ({@code sx, sy}).
     * <ol>
     *   <li>Raycasts against AABBs of the pickable scenes and returns the closest hit.</li>
     *   <li>If no object is hit, falls back to intersection with {@code mapPlane}.</li>
     * </ol>
     *
     * @param sx  screen X (as given by {@link com.badlogic.gdx.InputProcessor})
     * @param sy  screen Y (as given by {@link com.badlogic.gdx.InputProcessor})
     * @param out result vector: world point under the pixel
     * @return {@code true} if a world point was found, {@code false} otherwise
     */
    private boolean screenToWorldUnderPixel(int sx, int sy, Vector3 out) {
        Ray ray = camera.getPickRay(sx, sy); // no Y flip here for consistent dragging

        // 1) Closest object under pixel
        float bestT = Float.POSITIVE_INFINITY;
        boolean hasHit = false;

        for (Scene s : safeScenes()) {
            if (s == null || s.modelInstance == null) continue;

            // World-space bounding box (ensure transforms are updated before render)
            BoundingBox bb = new BoundingBox();
            s.modelInstance.calculateBoundingBox(bb);
            bb.mul(s.modelInstance.transform); // robustness: world-space box

            float t = intersectRayAABB(ray, bb);
            if (t >= 0f && t < bestT) {
                bestT = t;
                hasHit = true;
            }
        }

        if (hasHit) {
            out.set(ray.origin).mulAdd(ray.direction, bestT);
            return true;
        }

        // 2) Fallback: map plane intersection
        return Intersector.intersectRayPlane(ray, mapPlane, out);
    }

    /**
     * Ray vs AABB (slab method).
     *
     * @param ray  picking ray (origin + direction)
     * @param box  axis-aligned bounding box in world space
     * @return entry parameter {@code t} on the box (>=0), or {@code -1} if no intersection
     */
    private static float intersectRayAABB(Ray ray, BoundingBox box) {
        Vector3 min = box.min, max = box.max;
        float tmin = 0f;
        float tmax = Float.POSITIVE_INFINITY;

        // X
        if (Math.abs(ray.direction.x) < 1e-8f) {
            if (ray.origin.x < min.x || ray.origin.x > max.x) return -1f;
        } else {
            float tx1 = (min.x - ray.origin.x) / ray.direction.x;
            float tx2 = (max.x - ray.origin.x) / ray.direction.x;
            float txmin = Math.min(tx1, tx2);
            float txmax = Math.max(tx1, tx2);
            tmin = Math.max(tmin, txmin);
            tmax = Math.min(tmax, txmax);
            if (tmax < tmin) return -1f;
        }

        // Y
        if (Math.abs(ray.direction.y) < 1e-8f) {
            if (ray.origin.y < min.y || ray.origin.y > max.y) return -1f;
        } else {
            float ty1 = (min.y - ray.origin.y) / ray.direction.y;
            float ty2 = (max.y - ray.origin.y) / ray.direction.y;
            float tymin = Math.min(ty1, ty2);
            float tymax = Math.max(ty1, ty2);
            tmin = Math.max(tmin, tymin);
            tmax = Math.min(tmax, tymax);
            if (tmax < tmin) return -1f;
        }

        // Z
        if (Math.abs(ray.direction.z) < 1e-8f) {
            if (ray.origin.z < min.z || ray.origin.z > max.z) return -1f;
        } else {
            float tz1 = (min.z - ray.origin.z) / ray.direction.z;
            float tz2 = (max.z - ray.origin.z) / ray.direction.z;
            float tzmin = Math.min(tz1, tz2);
            float tzmax = Math.max(tz1, tz2);
            tmin = Math.max(tmin, tzmin);
            tmax = Math.min(tmax, tzmax);
            if (tmax < tmin) return -1f;
        }

        return tmin >= 0f ? tmin : (tmax >= 0f ? tmax : -1f);
    }

    /** @return pickable scenes, or an empty list if the provider is null */
    private Iterable<Scene> safeScenes() {
        if (provider == null) return new ArrayList<>();
        return provider.getPickableScenes();
    }
}
