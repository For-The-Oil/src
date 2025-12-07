package io.github.core.game_engine;

import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.graphics.Camera;

public class CameraController {

    private static final CameraController INSTANCE = new CameraController();

    public float zoomDelta = 0f;
    public float rotDelta = 0f;
    public boolean resetRequested = false;

    // Valeurs sûres par défaut
    private final Vector3 defaultPos = new Vector3(1500f, 2000f, 800f);
    private final Vector3 defaultTarget = new Vector3(1500f, 0f, 800f);

    private CameraController() {}

    public static CameraController get() {
        return INSTANCE;
    }

    public void applyToCamera(Camera cam) {

        if (resetRequested) {
            cam.position.set(defaultPos);
            if (cam.position.epsilonEquals(defaultTarget, 0.001f)) {
                cam.position.add(0.01f, 0f, 0.01f);
            }
            cam.lookAt(defaultTarget);
            //cam.up.set(Vector3.Y);
            cam.update();
            resetRequested = false;
        }

        if (zoomDelta != 0f) {
            Vector3 dir = new Vector3(defaultTarget).sub(cam.position).nor();
            cam.position.add(dir.scl(zoomDelta));
            zoomDelta = 0;
            cam.update();
        }

        if (rotDelta != 0f) {
            cam.rotateAround(defaultTarget, Vector3.Y, rotDelta);
            rotDelta = 0;
            cam.update();
        }
    }
}
