package io.github.android.gui.fragment.game;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.badlogic.gdx.backends.android.AndroidFragmentApplication;
import com.badlogic.gdx.math.Plane;
import com.badlogic.gdx.math.Vector3;

import io.github.android.activity.GameActivity;
import io.github.android.gui.GameRenderer;
import io.github.android.utils.CameraGestureController;
import io.github.shared.data.enums_types.EntityType;

public class LibGdxFragment extends AndroidFragmentApplication {

    private GameRenderer renderer;
    private Runnable pendingReadyCallback;
    private GameActivity gameActivity;

    public LibGdxFragment(GameActivity activity){
        super();
        gameActivity = activity;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();
        config.useGL30 = true;
        config.useImmersiveMode = true;

        // Empêche de garder le contexte GL actif si l'app passe en pause
        // Cela aide souvent à éviter le crash au resume/restart
        config.a = 8;
        config.b = 8;
        config.g = 8;
        config.r = 8;

        // On recrée toujours le renderer.
        // Si tu réutilises une instance Java d'un renderer qui contient des Textures d'un ancien contexte GL, ça crash.
        renderer = new GameRenderer(gameActivity);
        renderer.setOnCameraReady(this::setupCameraGestures);

        if (pendingReadyCallback != null) renderer.setOnLibGdxReady(pendingReadyCallback);

        return initializeForView(renderer, config);
    }

    private void setupCameraGestures() {
        // Vérification de sécurité
        if (renderer == null || renderer.getCamera() == null) return;

        Plane mapPlane = new Plane(new Vector3(0, 1, 0), 0f);

        // MODIFICATION ICI : On passe 'renderer' au lieu de 'renderer.getCamera()'
        // Cela correspond à la modification faite dans l'étape précédente.
        CameraGestureController gestures = new CameraGestureController(
            renderer,                     // <--- On passe l'instance du GameRenderer
            new Vector3(1500f, 0f, 800f), // Cible initiale
            mapPlane                      // Plan du sol
        );

        Gdx.input.setInputProcessor(gestures);
    }

    /**
     * Pas besoin d'appeler manuellement disposeGame ici.
     * AndroidFragmentApplication appellera renderer.dispose() automatiquement.
     */
    @Override
    public void onDestroyView() {
        // Désabonner l'input processor pour éviter les fuites
        if (Gdx.input != null) {
            Gdx.input.setInputProcessor(null);
        }

        super.onDestroyView(); // Cela va déclencher le dispose() du renderer
        this.renderer = null;  // Nettoyage de la référence
    }

    // Supprime la méthode disposeGame() qui est source d'erreurs.
    // Assure-toi juste que ton GameRenderer implémente bien ApplicationListener ou extends ApplicationAdapter
    // et qu'il nettoie ses textures dans sa méthode dispose().

    public GameRenderer getRenderer() {
        return renderer;
    }

    public void setOnLibGdxReady(Runnable r) {
        this.pendingReadyCallback = r;
        if (renderer != null)
            renderer.setOnLibGdxReady(r);
    }

    public Vector3 getWorldCenterCoordinates() {
        if (renderer == null || renderer.getCamera() == null) return new Vector3(0, 0, 0);

        // 1. Définir le centre de l'écran
        int centerX = Gdx.graphics.getWidth() / 2;
        int centerY = Gdx.graphics.getHeight() / 2;

        // 2. Créer le rayon et le plan de collision (Y = 0)
        com.badlogic.gdx.math.collision.Ray ray = renderer.getCamera().getPickRay(centerX, centerY);
        Plane mapPlane = new Plane(new Vector3(0, 1, 0), 0f);
        Vector3 intersection = new Vector3();

        // 3. Calculer l'intersection
        if (com.badlogic.gdx.math.Intersector.intersectRayPlane(ray, mapPlane, intersection)) {
            return intersection; // Contient X, Y=0, Z
        }

        return new Vector3(0, 0, 0);
    }

}
