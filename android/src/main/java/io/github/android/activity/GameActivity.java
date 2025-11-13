package io.github.android.activity;

import static io.github.android.config.ClientDefaultConfig.INIT_WAITING_TIME;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;

import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;

import io.github.android.gui.GameRenderer;
import io.github.android.gui.fragment.launcher.LoadingFragment;
import io.github.android.listeners.ClientListener;
import io.github.android.utils.NetworkUtils;
import io.github.core.game_engine.ClientLauncher;
import io.github.fortheoil.R;
import io.github.shared.local.data.requests.SynchronizeRequest;


/**
 * <h1>Game Activity</h1>
 *
 * Activity that starts when the client join a game.
 *
 */
public class GameActivity extends BaseActivity {

    private View loadingContainer;
    private FrameLayout libgdxContainer;
    private LoadingFragment loadingFragment;
    private ClientLauncher gameLogic;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.game);
        libgdxContainer = findViewById(R.id.libgdxContainer);
        loadingContainer = findViewById(R.id.loadingContainer);

        // Récupérer les infos de la game depuis l'Intent
        Bundle extras = getIntent().getExtras();

        Log.d("For The Oil", "Game started");

        initListener();
        setupLoadingFragment();

    }



















    // ----------
    // Launch Game
    // ----------

    private void beginGameStart(){
        loadingFragment.animateProgress(0,25,INIT_WAITING_TIME,"Initialisation",null,() -> {
            fullSyncAnimated();
        });
    }



    // ----------
    // Full-Resync animations
    // ----------


    private void fullSyncAnimated(){
        loadingFragment.animateProgress(50,75,INIT_WAITING_TIME,"Synchronizing", null, this::actualSync);
    }

    private void actualSync(){
        Log.d("For The Oil", "We are asking the server for synchronization !");
        NetworkUtils.askForFullGameSync();


        //on finish
        loadTexture();
    }



    // ----------
    // Textures & assets loadings
    // ----------

    private void loadTexture(){
        loadingFragment.animateProgress(25,50,INIT_WAITING_TIME,"Loading assets",null, null);
    }













    /**
     * Affiche l’overlay de chargement (XML)
     */
    public void showloadingContainer() {
        if (loadingContainer != null) loadingContainer.setVisibility(View.VISIBLE);
    }

    /**
     * Masque l’overlay de chargement (XML)
     */
    public void hideloadingContainer() {
        if (loadingContainer != null) loadingContainer.setVisibility(View.GONE);
    }













    public void libGdxInit(){
        // Configurer LibGDX
        AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();
        config.useAccelerometer = false;
        config.useCompass = false;
        config.useImmersiveMode = true;
        config.useGL30 = true;   // libGDX 3D nécessite OpenGL ES2+
        config.depth = 16;       // active le depth buffer

        // Créer la vue LibGDX
        AndroidApplication app = new AndroidApplication();
        View libgdxView = app.initializeForView(new GameRenderer(), config);
        libgdxContainer.addView(libgdxView);
    }

    private void setupLoadingFragment(){
        loadingFragment = new LoadingFragment();
        getSupportFragmentManager().beginTransaction()
            .add(R.id.loadingContainer, loadingFragment, "LOADING_FRAGMENT")
            .commit();
        FrameLayout overlay = findViewById(R.id.loadingContainer);
        overlay.setOnTouchListener((v, event) -> overlay.getVisibility() == View.VISIBLE);
        overlay.post(this::beginGameStart);
    }


    private void initListener(){
        ClientListener.getInstance().clearCallbacks();
        ClientListener.getInstance().setCurrentActivity(this);
        ClientListener.getInstance().onMessage(SynchronizeRequest.class, (request -> {



        }), true);
    }











    // -----
    // Getters & setters
    // -----


    public ClientLauncher getGameLogic() {
        return gameLogic;
    }

    public void setGameLogic(ClientLauncher gameLogic) {
        this.gameLogic = gameLogic;
    }



}
