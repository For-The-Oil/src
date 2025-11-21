package io.github.android.activity;

import static io.github.android.config.ClientDefaultConfig.INIT_WAITING_TIME;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import io.github.android.gui.adapter.MainAdapter;
import io.github.android.gui.fragment.launcher.LoadingFragment;
import io.github.android.gui.fragment.main.DeckFragment;
import io.github.android.gui.fragment.main.MainPageFragment;
import io.github.android.gui.fragment.main.MatchMakingFragment;
import io.github.android.listeners.ClientListener;
import io.github.android.manager.ClientManager;
import io.github.android.manager.MatchMakingManager;
import io.github.android.manager.SessionManager;
import io.github.android.utils.RedirectUtils;
import io.github.android.utils.UiUtils;
import io.github.fortheoil.R;
import io.github.shared.data.EnumsTypes.EntityType;
import io.github.shared.data.gameobject.Deck;
import io.github.shared.data.requests.DeckRequest;
import io.github.shared.data.requests.MatchMakingRequest;


/**
 * <h1>Home Activity</h1>
 *
 * <p>This activity is representation of the main menu. It's composed of 3 fragments :</p>
 * <ul>
 *     <li>Main Fragment : Where the player can start a game, disconnect, select the game mode ...</li>
 *     <li>Deck Fragment : Where we can select what card we want to bring to battle </li>
 *     <li>Stats Fragment : A placeholder fragment that should be used in order to load multiple usefull stats for the player</li>
 * </ul>
 *
 */
public class HomeActivity extends BaseActivity {

    private LinearLayout dotsLayout;
    private MainAdapter adapter;
    private ClientManager clientManager; // déclaration manquante

    public LoadingFragment loadingFragment;
    private MatchMakingFragment matchmakingFragment;

    private String gameMode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.second_activity);

        this.clientManager = ClientManager.getInstance();
        //this.clientManager.getKryoManager().addListener(new ClientListener());
        this.clientManager.setCurrentContext(this);
        //ClientListener.getInstance(this, null).setCurrentActivity(this);
        setupViewPager();
        setupLoadingFragment();
        setupMatchmakingFragment();

        initListener();
        addMatchMakingListener();
        addSessionUpdateListener();

        Log.d("For The Oil", "Decks: " + SessionManager.getInstance().getDecks());

        Deck defaultDeck = SessionManager.getInstance().getDecks().get("Default Deck");
        if (defaultDeck != null) {
            Log.d("For The Oil", "Default Deck: " + defaultDeck);
            Log.d("For The Oil", "Default Deck class: " + defaultDeck.getClass());
        } else {
            Log.d("For The Oil", "Default Deck is null");
        }

        String username = SessionManager.getInstance().getUsername();
        Log.d("For The Oil", "Username: " + (username != null ? username : "null"));



        ArrayList<EntityType> unlockedCards = SessionManager.getInstance().getUnlockedCards();
        if (unlockedCards != null) {
            for (EntityType type : unlockedCards) {
                Log.d("For The Oil", "Unlocked card: " + type.name());
            }
        } else {
            Log.d("For The Oil", "No unlocked cards");
        }

    }


    // -------------------------
    // Direct Link execution
    // -------------------------

    /**
     * This method is called when the player press the disconnect button on the main menu
     * @param view
     */
    public void disconnect(View view) {
        loadingFragment.show();
        loadingFragment.animateProgress(0f, 100f, INIT_WAITING_TIME, "Disconnecting...", null,
            () -> {
                SessionManager.getInstance().clearSession();
                clientManager.getKryoManager().closeClient();
                RedirectUtils.simpleRedirectAndClearStack(this, LoginActivity.class);
            });
    }


    // -------------------------
    // UI utils
    // -------------------------

    /**
     * This method allow the loading of the little dots at the bottom of the screen indicating on which fragment we are
     */
    private void setupViewPager() {
        ViewPager2 viewPager = findViewById(R.id.secondViewPager);
        dotsLayout = findViewById(R.id.dotsLayout);

        this.adapter = new MainAdapter(this);
        viewPager.setAdapter(adapter);

        int pageCount = adapter.getItemCount();

        viewPager.setCurrentItem(1, false);

        // Affiche les dots dès le départ
        viewPager.post(() -> {
            UiUtils.addBottomDots(HomeActivity.this, dotsLayout, viewPager.getCurrentItem(), pageCount);
        });

        // Mets à jour les dots quand on change de page
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                UiUtils.addBottomDots(HomeActivity.this, dotsLayout, position, pageCount);
            }
        });
    }


    /**
     * This method init the loading fragment for short animation / changing menu
     */
    private void setupLoadingFragment(){
        loadingFragment = new LoadingFragment();
        getSupportFragmentManager().beginTransaction()
            .add(R.id.loadingOverlay, loadingFragment, "LOADING_FRAGMENT")
            .commit();
        FrameLayout overlay = findViewById(R.id.loadingOverlay);
        overlay.setOnTouchListener((v, event) -> overlay.getVisibility() == View.VISIBLE);
    }

    /**
     * This method setup the matchmaking fragment, where the player wait for the server to give him a game.
     */
    private void setupMatchmakingFragment(){
        matchmakingFragment = new MatchMakingFragment();
        getSupportFragmentManager().beginTransaction()
            .add(R.id.matchmakingOverlay, matchmakingFragment, "MATCHMAKING_FRAGMENT")
            .commit();
    }

    /**
     * This method should be called in order to show the matchmaking overlay
     */
    public void showMatchmakingOverlay() {
        MatchMakingFragment fragment = (MatchMakingFragment) getSupportFragmentManager()
            .findFragmentByTag("MATCHMAKING_FRAGMENT");

        if (fragment != null) {
            fragment.show();
        }
    }

    /**
     * This method should be called in order to hide the matchmaking overlay
     */
    public void hideMatchmakingOverlay() {
        MatchMakingFragment fragment = (MatchMakingFragment) getSupportFragmentManager()
            .findFragmentByTag("MATCHMAKING_FRAGMENT");

        if (fragment != null) {
            fragment.hide();
        }
    }

    /**
     * This method is called when we need to start a new game
     * @param gameInfo
     */
    public void startNewGame(HashMap<String, String> gameInfo) {
        if (gameInfo == null || gameInfo.isEmpty()) return;
        RedirectUtils.simpleRedirect(this, GameActivity.class, gameInfo);
        //hideMatchmakingOverlay();
    }



    // -------------------------
    // Listeners utils
    // -------------------------

    /**
     * This method init the listeners of the main menu.
     */
    public void initListener(){
        ClientListener.getInstance().clearCallbacks();
        ClientListener.getInstance().setCurrentActivity(this);
    }

    /**
     * This method is called when the client want to join the matchmaking.
     * It checks if the server send a MatchMakingRequest to us.
     */
    public void addMatchMakingListener(){
        ClientListener myListener = ClientListener.getInstance();
        myListener.onMessage(MatchMakingRequest.class, request -> {
           Log.d("For The Oil", "Requete : "+request.getCommand().toString());
           switch (request.getCommand()){
               case CONFIRM:
                   MatchMakingManager.getInstance().confirmedMatchmaking();
                   break;

               case LEAVE:
                   MatchMakingManager.getInstance().leaveMatchmaking();
                   break;

               case FOUND:
                   startNewGame(request.getKeys());
                   break;

               case ACTUALIZE:
                   break;

               default:
                   Log.d("For The Oil","Unexpected reponses from the server !");
                   break;
           }
        },
        true);
    }

    public void addSessionUpdateListener() {
        ClientListener listener = ClientListener.getInstance();

        listener.onMessage(DeckRequest.class, request -> {
                Log.d("ForTheOil", "DeckRequest update received from server");

                // Logs complets de la requête
                Log.d("ForTheOil", "Keys in request:");
                for (Map.Entry<String, String> entry : request.getKeys().entrySet()) {
                    Log.d("ForTheOil", "  " + entry.getKey() + " -> " + entry.getValue());
                }

                // Mise à jour des decks
                String decksJson = request.getKeys().get("deck_data");
                if (decksJson != null) {
                    Log.d("ForTheOil", "Updating decks from JSON: " + decksJson);
                    SessionManager.getInstance().setDecksFromJson(decksJson);

                    Deck deck = null;
                    String deckName = null;
                    for (String key : SessionManager.getInstance().getDecks().keySet()) {
                        Deck value = SessionManager.getInstance().getDecks().get(key);
                        if (value != null && value.toString().equals(SessionManager.getInstance().getCurrentDeck().toString())){
                            deck = value;
                            deckName = key;
                        }
                    }
                    if(deck != null) SessionManager.getInstance().setCurrentDeck(deck,deckName);
                    else SessionManager.getInstance().setCurrentDeck(SessionManager.getInstance().getDecks().entrySet().iterator().next().getValue(),SessionManager.getInstance().getDecks().entrySet().iterator().next().getKey());

                }

                // Mise à jour des cartes débloquées
                String unlockedJson = request.getKeys().get("unlocked_cards");
                if (unlockedJson != null) {
                    Log.d("ForTheOil", "Updating unlocked cards from JSON: " + unlockedJson);
                    SessionManager.getInstance().setUnlockedCardsFromJson(unlockedJson);
                }

                // Mise à jour du deck courant
                String currentDeckName = request.getKeys().get("current_deck");
                if (currentDeckName != null && SessionManager.getInstance().getDecks().containsKey(currentDeckName)) {
                    Deck currentDeck = SessionManager.getInstance().getDecks().get(currentDeckName);
                    Log.d("ForTheOil", "Setting current deck to: " + currentDeckName + " -> " + currentDeck);
                    SessionManager.getInstance().setCurrentDeck(currentDeck,currentDeckName);
                }

                if (SessionManager.getInstance().getDecks().isEmpty()) {
                    SessionManager.getInstance().setCurrentDeck(null,null);
                }


                Fragment f = getSupportFragmentManager().findFragmentByTag("f0");
                if (f instanceof DeckFragment && f.getView() != null) {
                    runOnUiThread(() -> {
                        ((DeckFragment) f).refreshUI();
                        Log.d("ForTheOil", "refreshUI() called on DeckFragment");
                    });
                }

                Fragment f2 = this.getSupportFragmentManager().findFragmentByTag("f1");
                if (f2 instanceof MainPageFragment) {
                    ((MainPageFragment) f2).updateDeckButton();
                }
        }, true); // true = listener persistant
    }




    // -------------------------
    // Getters utils
    // -------------------------

    public void setGameMode(String mode) {
        gameMode = mode;
    }

    public String getGameMode() {
        return gameMode;
    }




}
