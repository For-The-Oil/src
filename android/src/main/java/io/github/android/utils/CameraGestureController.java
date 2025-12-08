package io.github.android.utils;

import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.Plane;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.collision.Ray;
import io.github.core.game_engine.CameraController;

public class CameraGestureController extends InputAdapter {
    private final Camera camera;
    private final Vector3 target;
    private final Plane mapPlane;

    // état
    private int pointer1 = -1, pointer2 = -1;
    private final Vector2 prev1 = new Vector2(), prev2 = new Vector2();
    private final Vector3 tmp = new Vector3();
    private final Vector3 anchorWorld = new Vector3();
    private boolean hasAnchor = false;

    // zoom
    private final float MIN_ZOOM = 500f;
    private final float MAX_ZOOM = 5000f;
    private final float ZOOM_SPEED_FACTOR = 10f;

    // lien avec le contrôleur global
    private final CameraController cc = CameraController.get();

    public CameraGestureController(Camera cam, Vector3 target, Plane mapPlane) {
        this.camera = cam;
        this.target = target.cpy();
        this.mapPlane = mapPlane;
    }

    private boolean screenToWorldOnPlane(int x, int y, Vector3 out) {
        Ray ray = camera.getPickRay(x, y);
        return Intersector.intersectRayPlane(ray, mapPlane, out);
    }

    @Override
    public boolean touchDown(int x, int y, int pointer, int button) {
        if (pointer1 == -1) {
            pointer1 = pointer;
            prev1.set(x, y);
            hasAnchor = screenToWorldOnPlane(x, y, anchorWorld); // ancre sur le doigt 1
            cc.setInputLock(true);                               // verrouiller les modifs externes
        } else if (pointer2 == -1) {
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
            hasAnchor = false;
            cc.setLookTarget(target);    // pousser la cible courante au contrôleur
            cc.setInputLock(false);      // libérer les modifs externes
        }
        return true;
    }

    @Override
    public boolean touchDragged(int x, int y, int pointer) {
        if (camera == null) return false;

        // --- 1 doigt : garder l’ancre sous pointer1
        if (pointer1 != -1 && pointer2 == -1) {
            Vector2 curr = (pointer == pointer1) ? new Vector2(x, y) : prev1.cpy();
            Vector3 worldUnderFinger = new Vector3();

            if (hasAnchor && screenToWorldOnPlane(Math.round((int)curr.x), Math.round((int)curr.y), worldUnderFinger)) {
                tmp.set(anchorWorld).sub(worldUnderFinger); // translation monde
                camera.position.add(tmp);
                target.add(tmp);
                camera.lookAt(target);
                camera.update();
            }
            prev1.set(curr);
        }

        // --- 2 doigts : pinch + garder l’ancre sous pointer1
        else if (pointer1 != -1 && pointer2 != -1) {
            Vector2 curr1 = (pointer == pointer1) ? new Vector2(x, y) : prev1.cpy();
            Vector2 curr2 = (pointer == pointer2) ? new Vector2(x, y) : prev2.cpy();

            // 1) zoom (distance cam-target clampée)
            float prevDist = prev1.dst(prev2);
            float currDist = curr1.dst(curr2);
            float zoomAmount = (prevDist - currDist) * ZOOM_SPEED_FACTOR;

            Vector3 offset = tmp.set(camera.position).sub(target);
            float currentDistance = offset.len();
            float targetDistance = Math.max(MIN_ZOOM, Math.min(MAX_ZOOM, currentDistance + zoomAmount));
            offset.nor().scl(targetDistance);
            camera.position.set(target).add(offset);

            // 2) corriger pour que l’ancre reste sous pointer1
            Vector3 worldUnderFinger1 = new Vector3();
            if (hasAnchor && screenToWorldOnPlane(Math.round((int)curr1.x), Math.round((int)curr1.y), worldUnderFinger1)) {
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
}
