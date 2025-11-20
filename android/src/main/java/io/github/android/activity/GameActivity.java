package io.github.android.activity;

import static io.github.android.config.ClientDefaultConfig.INIT_WAITING_TIME;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;

import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;

import java.util.ArrayList;

import io.github.android.gui.GameRenderer;
import io.github.android.gui.fragment.launcher.LoadingFragment;
import io.github.android.listeners.ClientListener;
import io.github.android.manager.ClientManager;
import io.github.android.utils.NetworkUtils;
import io.github.android.utils.OtherUtils;
import io.github.core.game_engine.ClientGame;
import io.github.core.game_engine.ClientLauncher;
import io.github.fortheoil.R;
import io.github.shared.data.NetGame;
import io.github.shared.data.instructions.Instruction;
import io.github.shared.data.requests.SynchronizeRequest;


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
    private ClientLauncher clientLauncher;
    private ClientGame clientGame;
    private ClientManager clientManager = ClientManager.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.game);
        clientManager.setCurrentContext(this);
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
        new Thread(() -> {
            NetworkUtils.askForFullGameSync();
        }).start();
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

            switch (request.getType()) {

                case INSTRUCTION_SYNC:
                    Log.d("For The Oil","Instruction Request received :"+request.getType().toString());
//                    clientLauncher.addQueueInstruction();
                    break;

                case FULL_RESYNC:
                    Log.d("For The Oil","FullSynchronizeRequest received :"+request.getType().toString());
                    NetGame netGame = (NetGame) request.getMap().get("game");

                    clientGame = OtherUtils.clientGameBuilder(netGame);

                    // TODO : Create a client launcher and ClientGame made from the data sent by the server
                    // TODO : We MUST check if the data is correct

                    //clientLauncher.setResyncNetGame(netGame);


                    break;

                default:
                    break;
            }

        }), true);
    }










    // -----
    // Getters & setters
    // -----


    public ClientLauncher getClientLauncher() {
        return clientLauncher;
    }

    public void setClientLauncher(ClientLauncher clientLauncher) {
        this.clientLauncher = clientLauncher;
    }



}
