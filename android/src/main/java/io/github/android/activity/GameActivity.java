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
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import io.github.android.gui.fragment.game.LibGdxFragment;
import io.github.android.gui.fragment.launcher.LoadingFragment;
import io.github.android.listeners.ClientListener;
import io.github.android.manager.ClientManager;
import io.github.android.manager.MatchMakingManager;
import io.github.android.manager.SessionManager;
import io.github.android.utils.NetworkUtils;
import io.github.android.utils.UiUtils;
import io.github.core.data.ClientGame;
import io.github.core.game_engine.ClientLauncher;
import io.github.core.game_engine.factory.ModelFactory;
import io.github.core.game_engine.factory.SceneFactory;
import io.github.core.game_engine.manager.GameManager;
import io.github.fortheoil.R;
import io.github.shared.data.NetGame;
import io.github.shared.data.enums_types.DeckCardCategory;
import io.github.shared.data.enums_types.EntityType;
import io.github.shared.data.enums_types.EventType;
import io.github.shared.data.enums_types.ResourcesType;
import io.github.shared.data.gameobject.Deck;
import io.github.shared.data.instructions.EventsInstruction;
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

        // Récupérer les infos de la game depuis l'Intent
        Bundle extras = getIntent().getExtras();

        Log.d("For The Oil", "Game started");


        initListener();
        setupLoadingFragment();
        setupToggleMenuButton();
        setupBuildingButton();
        initSettings();
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
        libGdxFragment = new LibGdxFragment();
        getSupportFragmentManager()
            .beginTransaction()
            .replace(R.id.libgdxContainer, libGdxFragment)
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

                    if(loadingFragment.isVisible()) successSync();
                    break;

                default:
                    break;
            }

        }), true);
    }



    private void setupToggleMenuButton() {
        ImageButton btnToggleMenu = findViewById(R.id.btnToggleMenu);
        LinearLayout bottomPanel = findViewById(R.id.bottomPanel);

        btnToggleMenu.setOnClickListener(v -> {
            if (bottomPanel.getVisibility() == View.VISIBLE) {
                bottomPanel.setVisibility(View.GONE);
                btnToggleMenu.setImageResource(R.drawable.keyboard_double_arrow_up_24px);
            } else {
                bottomPanel.setVisibility(View.VISIBLE);
                btnToggleMenu.setImageResource(R.drawable.keyboard_double_arrow_down_24px);
            }
        });

    }


    private void setupBuildingButton(){
        Button btnIndustry = findViewById(R.id.btnIndustry);
        Button btnMilitary = findViewById(R.id.btnMilitary);
        Button btnDefense  = findViewById(R.id.btnDefense);

        btnIndustry.setOnClickListener(v ->
            showCardsForCategory(DeckCardCategory.Industrial));

        btnMilitary.setOnClickListener(v ->
            showCardsForCategory(DeckCardCategory.Military));

        btnDefense.setOnClickListener(v ->
            showCardsForCategory(DeckCardCategory.Defense));

    }


    private void showCardsForCategory(DeckCardCategory category) {
        Deck deck = SessionManager.getInstance().getCurrentDeck();

        if (deck == null) {
            Log.e("ForTheOil", "No current deck is selected!");
            return;
        }

        List<EntityType> cards = deck.getCardsByCategory().get(category);

        if (cards == null) cards = new ArrayList<>();

        Log.d("ForTheOil", "Cards in " + category + ": " + cards.toString());

        updateRightPanel(cards);
    }


    private void updateRightPanel(List<EntityType> cards) {
        FlexboxLayout rightPanel = findViewById(R.id.contentContainer); // Flexbox pour multi-colonnes
        rightPanel.removeAllViews();

        LayoutInflater inflater = LayoutInflater.from(this);

        for (EntityType card : cards) {
            View cardView = inflater.inflate(R.layout.right_panel_card, rightPanel, false);

            ImageView img = cardView.findViewById(R.id.cardImage);
            LinearLayout costsLayout = cardView.findViewById(R.id.cardCosts);

            img.setImageResource(UiUtils.mapEntityTypeToDrawable(card));

            // Ajouter chaque coût comme TextView
            costsLayout.removeAllViews();
            HashMap<ResourcesType, Integer> costMap = card.getCost();
            if (costMap != null) {
                for (Map.Entry<ResourcesType, Integer> entry : costMap.entrySet()) {
                    TextView costView = new TextView(this);
                    costView.setText(entry.getKey().name() + ": " + entry.getValue());
                    costView.setTextSize(10f);
                    costView.setTextColor(Color.BLACK);
                    costView.setGravity(Gravity.CENTER);
                    costsLayout.addView(costView);
                }
            }

            rightPanel.addView(cardView);
        }
    }


    private void initSettings(){
        // Overlay complet
        FrameLayout settingsOverlay = findViewById(R.id.gameSettingsOverlay);
        LinearLayout settingsMenu = findViewById(R.id.settingsMenu);

        // Boutons
        Button btnToggleMusic = findViewById(R.id.btnToggleMusic);
        Button btnToggleSfx = findViewById(R.id.btnToggleSfx);
        Button btnQuit = findViewById(R.id.btnQuit);
        Button btnAbandon = findViewById(R.id.btnAbandon);
        Button btnReturn = findViewById(R.id.btnReturn);

        // Bouton engrenage
        ImageButton btnSettings = findViewById(R.id.btnSettings);


        // Afficher le menu quand on clique sur l'engrenage
        btnSettings.setOnClickListener(v -> settingsOverlay.setVisibility(View.VISIBLE));

        // Fermer le menu si clic en dehors du menu central
        settingsOverlay.setOnClickListener(v -> settingsOverlay.setVisibility(View.GONE));

        // Éviter que le clic dans le menu ferme l'overlay
        settingsMenu.setOnClickListener(v -> {
            // Ne rien faire
        });

        // Actions des boutons
        btnToggleMusic.setOnClickListener(v -> {
            // TODO: activer/désactiver la musique
        });

        btnToggleSfx.setOnClickListener(v -> {
            // TODO: activer/désactiver les effets sonores
        });

        // Dans GameActivity.java

        btnQuit.setOnClickListener(v -> {
            // 1. Masquer l'interface
            if (libgdxContainer != null) {
                libgdxContainer.setVisibility(View.GONE);
            }

            // 2. Supprimer le fragment proprement
            if (libGdxFragment != null) {

                ClientGame.getInstance().setRunning(false);

                getSupportFragmentManager()
                    .beginTransaction()
                    .remove(libGdxFragment) // L'enlèvement du fragment appelle GameRenderer.dispose()
                    .commitNowAllowingStateLoss(); // Force l'exécution immédiate
            }

            // 3. Nettoyer les managers
            MatchMakingManager.getInstance().resetMatchmaking();

            // 4. L'appel finish() sera fait APRES que GameRenderer.dispose() (maintenant synchrone)
            // ait terminé son travail.
            finish();
        });

        btnAbandon.setOnClickListener(v -> {
            // TODO: abandonner la partie
        });

        btnReturn.setOnClickListener(v -> {
            // Fermer le menu
            settingsOverlay.setVisibility(View.GONE);
        });



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
