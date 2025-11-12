package io.github.android.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;

import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;

import io.github.android.gui.GameRenderer;
import io.github.fortheoil.R;


/**
 * <h1>Game Activity</h1>
 *
 * Activity that starts when the client join a game.
 *
 */
public class GameActivity extends AndroidApplication {

    private View loadingOverlay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Charge ton layout XML qui contient libgdxContainer + overlay
        setContentView(R.layout.game);

        // Récupération du container LibGDX défini dans le XML
        FrameLayout libgdxContainer = findViewById(R.id.libgdxContainer);
        loadingOverlay = findViewById(R.id.loadingOverlay);

        // Récupérer les infos de la game depuis l'Intent
        Bundle extras = getIntent().getExtras();
        String gameUUID = extras != null ? extras.getString("game_uuid") : null;
        String mapName = extras != null ? extras.getString("map_name") : null;
        int maxPlayers = extras != null ? extras.getInt("max_players", 0) : 0;

        // Configurer LibGDX
        AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();
        config.useAccelerometer = false;
        config.useCompass = false;
        config.useImmersiveMode = true;
        config.useGL30 = true;   // libGDX 3D nécessite OpenGL ES2+
        config.depth = 16;       // active le depth buffer

        // Créer la vue LibGDX
        View libgdxView = initializeForView(new GameRenderer(gameUUID, mapName, maxPlayers), config);
        libgdxContainer.addView(libgdxView);
    }

    /**
     * Affiche l’overlay de chargement (XML)
     */
    public void showLoadingOverlay() {
        if (loadingOverlay != null) loadingOverlay.setVisibility(View.VISIBLE);
    }

    /**
     * Masque l’overlay de chargement (XML)
     */
    public void hideLoadingOverlay() {
        if (loadingOverlay != null) loadingOverlay.setVisibility(View.GONE);
    }
}
