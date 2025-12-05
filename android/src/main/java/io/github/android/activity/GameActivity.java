package io.github.android.activity;

import static io.github.android.config.ClientDefaultConfig.INIT_WAITING_TIME;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;

import com.badlogic.gdx.backends.android.AndroidFragmentApplication;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import io.github.android.gui.fragment.game.LibGdxFragment;
import io.github.android.gui.fragment.launcher.LoadingFragment;
import io.github.android.listeners.ClientListener;
import io.github.android.manager.ClientManager;
import io.github.android.utils.NetworkUtils;
import io.github.core.data.ClientGame;
import io.github.core.game_engine.ClientLauncher;
import io.github.core.game_engine.manager.GameManager;
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
public class GameActivity extends BaseActivity implements AndroidFragmentApplication.Callbacks{

    private View loadingContainer;
    private FrameLayout libgdxContainer;
    private LoadingFragment loadingFragment;
    private ClientLauncher clientLauncher;
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

    @Override
    public void exit() {
        finish();
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
        loadingFragment.animateProgress(50, 75, INIT_WAITING_TIME, "Synchronizing", null, this::actualSync);
    }

    private void actualSync(){
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.schedule(() -> {
            if(ClientGame.isInstanceNull()) {
                Log.d("For The Oil", "We are asking the server for synchronization !");
                NetworkUtils.askForFullGameSync();
            }else Log.d("For The Oil", "There is no need to request for synchronization !");
            scheduler.shutdown();
        }, 1000, TimeUnit.MILLISECONDS);
    }


    private void successSync(){
        loadingFragment.animateProgress(75,80,INIT_WAITING_TIME,"Sync succeed", null, this::loadTexture);
    }

    private void failureSync(){
        loadingFragment.animateProgress(75,100,INIT_WAITING_TIME,"Sync failure !", null, null);
    }





    // ----------
    // Textures & assets loadings
    // ----------

    private void loadTexture(){
        loadingFragment.animateProgress(80,95,INIT_WAITING_TIME,"Loading assets",null, this::gameStarting);
    }


    private void gameStarting(){
        libGdxInit();
        loadingFragment.animateProgress(95,100,INIT_WAITING_TIME,"Starting !",null, () -> {
            hideloadingContainer();
        });
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

    private void libGdxInit() {
        getSupportFragmentManager()
            .beginTransaction()
            .replace(R.id.libgdxContainer, new LibGdxFragment())
            .commit();
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

                    Object obj = request.getMap().getOrDefault("instructions", null);
                    if(obj instanceof Queue){
                        // WARNING THIS PART COULD BE DANGEROUS
                        Queue<Instruction> queue = (Queue<Instruction>) obj;
                        clientLauncher.addQueueInstruction(queue);
                    }
                    break;

                case FULL_RESYNC:
                    Log.d("For The Oil","FullSynchronizeRequest received :"+request.getType().toString());
                    NetGame netGame = (NetGame) request.getMap().get("game");

                    if(clientLauncher==null){
                        GameManager.fullGameResync(netGame);
                        clientLauncher = new ClientLauncher();
                    }
                    else clientLauncher.setResyncNetGame(netGame);

                    if(loadingFragment.isVisible()) successSync();
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
