package io.github.android.gui.fragment.game;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.badlogic.gdx.backends.android.AndroidFragmentApplication;
import com.badlogic.gdx.math.Plane;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.Gdx;

import io.github.android.gui.GameRenderer;
import io.github.android.utils.CameraGestureController;

public class LibGdxFragment extends AndroidFragmentApplication {

    private GameRenderer renderer;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();
        config.useGL30 = true;
        config.useImmersiveMode = true;

        GameRenderer renderer = new GameRenderer();
        renderer.setOnCameraReady(() -> {
            Plane mapPlane = new Plane(new Vector3(0, 1, 0), 0f); // y = 0
            CameraGestureController gestures =
                new CameraGestureController(renderer.getCamera(),
                    new Vector3(1500f, 0f, 800f),
                    mapPlane);
            Gdx.input.setInputProcessor(gestures);
        });

        return initializeForView(renderer, config);
    }

    public GameRenderer getRenderer() {
        return renderer;
    }
}
