package io.github.android.utils;

import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.graphics.Camera;

public class CameraGestureController extends InputAdapter {

    private final Camera camera;
    private final Vector3 target;
    private final Vector3 tmp = new Vector3();
    private final Vector3 right = new Vector3();
    private final Vector3 up = new Vector3();

    private int pointer1 = -1;
    private int pointer2 = -1;

    private final Vector2 prev1 = new Vector2();
    private final Vector2 prev2 = new Vector2();

    // Constantes faciles à modifier
    private final float MIN_ZOOM = 100f;       // distance minimale
    private final float MAX_ZOOM = 5000f;      // distance maximale
    private final float PAN_SPEED_FACTOR = 0.002f; // facteur de vitesse du pan
    private final float ZOOM_SPEED_FACTOR = 0.5f;  // sensibilité du zoom
    private final float ZOOM_LERP_FACTOR = 0.15f;  // interpolation du zoom (0.1 = doux, 0.3 = rapide)

    public CameraGestureController(Camera cam, Vector3 target) {
        this.camera = cam;
        this.target = target.cpy();
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        if (pointer1 == -1) {
            pointer1 = pointer;
            prev1.set(screenX, screenY);
        } else if (pointer2 == -1) {
            pointer2 = pointer;
            prev2.set(screenX, screenY);
        }
        return true;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        if (pointer == pointer1) pointer1 = -1;
        else if (pointer == pointer2) pointer2 = -1;
        return true;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        if (camera == null) return false;

        // ----- ONE FINGER PAN -----
        if (pointer1 != -1 && pointer2 == -1) {
            Vector2 prev = (pointer == pointer1) ? prev1 : prev2;
            float dx = (screenX - prev.x);
            float dy = (screenY - prev.y);

            float distance = camera.position.dst(target);
            float panSpeed = distance * PAN_SPEED_FACTOR;

            right.set(camera.direction).crs(camera.up).nor();
            up.set(camera.up).nor();

            tmp.set(right).scl(-dx * panSpeed).add(up.scl(dy * panSpeed));
            camera.position.add(tmp);
            target.add(tmp);

            camera.lookAt(target);
            camera.update();
            prev.set(screenX, screenY);
        }
        // ----- TWO FINGER PAN + ZOOM -----
        else if (pointer1 != -1 && pointer2 != -1) {
            Vector2 curr1 = (pointer == pointer1) ? new Vector2(screenX, screenY) : prev1.cpy();
            Vector2 curr2 = (pointer == pointer2) ? new Vector2(screenX, screenY) : prev2.cpy();

            // --- Zoom ---
            float prevDist = prev1.dst(prev2);
            float currDist = curr1.dst(curr2);
            float zoomAmount = (prevDist - currDist) * ZOOM_SPEED_FACTOR;

            Vector3 offset = tmp.set(camera.position).sub(target);
            float currentDistance = offset.len();

            float targetDistance = Math.max(MIN_ZOOM, Math.min(MAX_ZOOM, currentDistance + zoomAmount));
            float newDistance = currentDistance + (targetDistance - currentDistance) * ZOOM_LERP_FACTOR;

            offset.nor().scl(newDistance);
            camera.position.set(target).add(offset);

            // --- Pan (midpoint) ---
            float prevMidX = (prev1.x + prev2.x) * 0.5f;
            float prevMidY = (prev1.y + prev2.y) * 0.5f;
            float currMidX = (curr1.x + curr2.x) * 0.5f;
            float currMidY = (curr1.y + curr2.y) * 0.5f;

            float dx = (currMidX - prevMidX);
            float dy = (currMidY - prevMidY);

            float distance = camera.position.dst(target);
            float panSpeed = distance * PAN_SPEED_FACTOR;

            right.set(camera.direction).crs(camera.up).nor();
            up.set(camera.up).nor();
            tmp.set(right).scl(-dx * panSpeed).add(up.scl(dy * panSpeed));
            camera.position.add(tmp);
            target.add(tmp);

            camera.lookAt(target);
            camera.update();

            prev1.set(curr1);
            prev2.set(curr2);
        }

        return true;
    }
}
