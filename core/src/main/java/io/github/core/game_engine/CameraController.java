
package io.github.core.game_engine;

import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.graphics.Camera;

public class CameraController {
    private static final CameraController INSTANCE = new CameraController();

    public float zoomDelta = 0f;
    public float rotDelta = 0f;
    public boolean resetRequested = false;

    private final Vector3 defaultPos = new Vector3(1500f, 2000f, 800f);
    private final Vector3 defaultTarget = new Vector3(1500f, 0f, 800f);

    // AJOUTS
    private final Vector3 lookTarget = new Vector3(defaultTarget);
    private boolean inputLock = false;

    private CameraController(){}

    public static CameraController get(){ return INSTANCE; }

    // AJOUTS
    public void setLookTarget(Vector3 t){ this.lookTarget.set(t); }
    public void setInputLock(boolean lock){ this.inputLock = lock; }

    public void applyToCamera(Camera cam) {
        // ignorer toute modification pendant un geste
        if (inputLock) return;

        if (resetRequested) {
            cam.position.set(defaultPos);
            cam.lookAt(defaultTarget);
            lookTarget.set(defaultTarget);
            cam.update();
            resetRequested = false;
        }

        if (zoomDelta != 0f) {
            // zoom le long de la direction actuelle
            Vector3 forward = new Vector3(cam.direction).nor();
            cam.position.add(forward.scl(zoomDelta));
            zoomDelta = 0f;
            cam.lookAt(lookTarget);
            cam.update();
        }

        if (rotDelta != 0f) {
            // rotation autour du lookTarget courant
            cam.rotateAround(lookTarget, Vector3.Y, rotDelta);
            rotDelta = 0f;
            cam.lookAt(lookTarget);
            cam.update();
        }
    }
}
