package io.github.android.activity;

import static io.github.android.config.ClientDefaultConfig.INIT_WAITING_TIME;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.badlogic.gdx.backends.android.AndroidFragmentApplication;
import com.google.android.flexbox.FlexboxLayout;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import io.github.android.gui.fragment.game.BottomFragment;
import io.github.android.gui.fragment.game.LibGdxFragment;
import io.github.android.gui.fragment.game.SettingsFragment;
import io.github.android.gui.fragment.launcher.LoadingFragment;
import io.github.android.listeners.ClientListener;
import io.github.android.manager.ClientManager;
import io.github.core.client_engine.manager.SessionManager;
import io.github.android.utils.NetworkUtils;
import io.github.android.utils.UiUtils;
import io.github.core.data.ClientGame;
import io.github.core.game_engine.ClientLauncher;
import io.github.core.game_engine.factory.ModelFactory;
import io.github.core.game_engine.factory.SceneFactory;
import io.github.core.game_engine.manager.GameManager;
import io.github.core.game_engine.system.GraphicsSyncSystem;
import io.github.fortheoil.R;
import io.github.shared.data.NetGame;
import io.github.shared.data.enums_types.DeckCardCategory;
import io.github.shared.data.enums_types.EntityType;
import io.github.shared.data.enums_types.EventType;
import io.github.shared.data.enums_types.ResourcesType;
import io.github.shared.data.gameobject.Deck;
import io.github.shared.data.instructions.Instruction;
import io.github.shared.data.instructions.EventsInstruction;
import io.github.shared.data.requests.SynchronizeRequest;

/**
 * <h1>Game Activity - Full Rework with DecisionTree Loading</h1>
 */
public class GameActivity extends BaseActivity implements AndroidFragmentApplication.Callbacks {

    private View loadingContainer;
    private FrameLayout libgdxContainer;
    private LoadingFragment loadingFragment;
    private LibGdxFragment libGdxFragment;
    private ClientLauncher clientLauncher;
    private ClientManager clientManager = ClientManager.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.game);

        clientManager.setCurrentContext(this);
        libgdxContainer = findViewById(R.id.libgdxContainer);
        loadingContainer = findViewById(R.id.loadingContainer);

        showLoadingContainer();
        setupLoadingFragment();
        initListener();
        initUI();
    }

    @Override
    public void exit() {
        finish();
    }

    /* ===================== Loading Decision Tree ===================== */

    private void stepInitialization() {
        loadingFragment.animateProgress(0, 25, INIT_WAITING_TIME, "Initialisation", null, this::stepPrepareSync);
    }

    private void stepPrepareSync() {
        loadingFragment.animateProgress(25, 50, INIT_WAITING_TIME, "Preparing sync", null, this::stepRequestSync);
    }

    private void stepRequestSync() {
        if (ClientGame.isInstanceNull()) {
            Log.d("For The Oil", "We are asking the server for synchronization !");
            NetworkUtils.askForFullGameSync();
        } else {
            Log.d("For The Oil", "There is no need to request for synchronization !");
            stepSyncSuccess();
        }
    }

    private void stepSyncSuccess() {
        loadingFragment.animateProgress(50, 65, INIT_WAITING_TIME, "Sync succeed", null, this::stepCreateLibGdx);
    }

    private void stepSyncFailure() {
        loadingFragment.animateProgress(50, 100, INIT_WAITING_TIME, "Sync failed! Retrying...", null, this::stepRequestSync);
    }

    private void stepCreateLibGdx() {
        libGdxFragment = new LibGdxFragment(this);
        libGdxFragment.setOnLibGdxReady(() -> runOnUiThread(this::stepOpenGLReady));
        getSupportFragmentManager().beginTransaction()
            .replace(R.id.libgdxContainer, libGdxFragment)
            .commit();
    }

    private void stepOpenGLReady() {
        loadingFragment.animateProgress(65, 100, INIT_WAITING_TIME, "Loading game", null, this::finishLoading);
    }

    private void finishLoading() {
        hideLoadingContainer();
        Log.d("GameActivity", "Game fully loaded and ready!");
    }

    /* ===================== UI Helpers ===================== */

    private void showLoadingContainer() {
        if (loadingContainer != null) loadingContainer.setVisibility(View.VISIBLE);
    }

    private void hideLoadingContainer() {
        if (loadingContainer != null) loadingContainer.setVisibility(View.GONE);
    }

    private void setupLoadingFragment() {
        loadingFragment = new LoadingFragment();
        getSupportFragmentManager().beginTransaction()
            .add(R.id.loadingContainer, loadingFragment, "LOADING_FRAGMENT")
            .runOnCommit(this::stepInitialization)
            .commit();
        FrameLayout overlay = findViewById(R.id.loadingContainer);
        overlay.setOnTouchListener((v, event) -> overlay.getVisibility() == View.VISIBLE);
    }



    private void initListener() {
        ClientListener.getInstance().clearCallbacks();
        ClientListener.getInstance().setCurrentActivity(this);

        ClientListener.getInstance().onMessage(SynchronizeRequest.class, request -> {
            switch (request.getType()) {
                case INSTRUCTION_SYNC:
                    Log.d("For The Oil","Instruction Request received :"+request.getType().toString());
                    Object obj = request.getMap().getOrDefault("instructions", null);
                    if(obj instanceof Queue){
                        Queue<Instruction> queue = (Queue<Instruction>) obj;
                        clientLauncher.addQueueInstruction(queue);
                        if(!clientLauncher.isAlive()){
                            Instruction instruction = queue.poll();
                            if(instruction instanceof EventsInstruction&&((EventsInstruction)instruction).getEventType().equals(EventType.START)){
                                clientLauncher.start();
                                Log.d("For The Oil","Instruction start received :");
                            }
                        }
                    }
                    break;

                case FULL_RESYNC:
                    Log.d("For The Oil","FullSynchronizeRequest received :"+request.getType().toString());
                    NetGame netGame = (NetGame) request.getMap().get("game");
                    if(clientLauncher==null){
                        GameManager.fullGameResync(netGame);
                        clientLauncher = new ClientLauncher();
                        if(ClientGame.getInstance().getCurrentEvent().equals(EventType.START)){
                            clientLauncher.start();
                            Log.d("For The Oil","ClientLauncher init and Instruction start received :");
                        }
                    }
                    else clientLauncher.setResyncNetGame(netGame);
                    if(loadingFragment.isVisible() && loadingFragment.getSplashProgress().getProgress() >= 50) stepSyncSuccess();
                    break;

                default:
                    break;
            }
        }, true);
    }

    private void openSettingsFragment() {
        FrameLayout container = findViewById(R.id.settingsFragmentContainer);
        container.setVisibility(View.VISIBLE);

        getSupportFragmentManager().beginTransaction()
            .replace(R.id.settingsFragmentContainer, new SettingsFragment())
            .commit();

        // Fermer si on clique sur l’overlay
        container.setOnClickListener(v -> closeSettingsFragment());
    }


    public void closeSettingsFragment() {
        FrameLayout container = findViewById(R.id.settingsFragmentContainer);

        getSupportFragmentManager().beginTransaction()
            .remove(getSupportFragmentManager().findFragmentById(R.id.settingsFragmentContainer))
            .commit();

        container.setVisibility(View.GONE);
    }




    public void quitGame() {
        showLoadingContainer();
        loadingFragment.animateProgress(0, 100, INIT_WAITING_TIME, "Retour au menu principal", null, () -> {
            if (libgdxContainer != null) libgdxContainer.setVisibility(View.GONE);
            if (libGdxFragment != null) {
                ClientGame.getInstance().setRunning(false);
                getSupportFragmentManager().beginTransaction().remove(libGdxFragment).commitNowAllowingStateLoss();
                libGdxFragment = null;
            }
            SceneFactory.disposeINSTANCE();
            ModelFactory.disposeINSTANCE();
            if (!ClientGame.isInstanceNull()) ClientGame.disposeInstance();
            finish();
        });
    }



    public void initUI() {
        initSettingsButton();
        initBottomFragment();
    }

    private void initBottomFragment() {
        BottomFragment bottomFragment = new BottomFragment();
        getSupportFragmentManager().beginTransaction()
            .replace(R.id.bottomFragmentContainer, bottomFragment)
            .commit();
    }

    public void initSettingsButton(){
        ImageButton btnSettings = findViewById(R.id.btnSettings);
        btnSettings.setOnClickListener(v -> openSettingsFragment());
    }


    public static void updateUI(){
        //TODO : Update the value in the UI
    }

}
