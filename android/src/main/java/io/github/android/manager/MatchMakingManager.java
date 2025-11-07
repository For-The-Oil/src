package io.github.android.manager;

import android.util.Log;

import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.github.android.activity.HomeActivity;
import io.github.android.gui.fragment.main.MatchMakingFragment;
import io.github.android.listeners.ClientListener;
import io.github.core.client_engine.factory.KryoMessagePackager;
import io.github.shared.local.data.EnumsTypes.GameModeType;
import io.github.shared.local.data.EnumsTypes.MatchModeType;
import io.github.shared.local.data.network.KryoMessage;
import io.github.shared.local.data.requests.MatchMakingRequest;

public class MatchMakingManager {

    public static MatchMakingManager INSTANCE;
    private final ExecutorService networkExecutor = Executors.newSingleThreadExecutor();
    private GameModeType gameMode;

    private MatchMakingManager(){}

    public static  MatchMakingManager getInstance(){
        if(INSTANCE==null) INSTANCE = new MatchMakingManager();
        return INSTANCE;
    }

    public void askMatchmaking(GameModeType gameMode){
        this.gameMode = gameMode;
        networkExecutor.execute(() -> {
            HashMap<String,String> myMap = new HashMap<>();
            MatchMakingRequest myRequest = new MatchMakingRequest(MatchModeType.ASK, gameMode, myMap);
            KryoMessage message = KryoMessagePackager.packAuthRequest(myRequest, SessionManager.getInstance().getToken());
            ClientManager.getInstance().getKryoManager().send(message);
        });
    }


    public void cancelMatchmaking(){
        networkExecutor.execute(() -> {
            HashMap<String, String> myMap = new HashMap<>();
            MatchMakingRequest myRequest = new MatchMakingRequest(MatchModeType.CANCEL, gameMode, myMap);
            KryoMessage message = KryoMessagePackager.packAuthRequest(myRequest, SessionManager.getInstance().getToken());
            ClientManager.getInstance().getKryoManager().send(message);
            Log.d("For The Oil", "Token is :" + SessionManager.getInstance().getToken());
            Log.d("For The Oil", "Cancel Matchmaking KryoMessage Sent !" + gameMode.toString());
        });
    }

    public void confirmedMatchmaking() {
        Log.d("For The Oil", "The matchmaking has been confirmed !");

        HomeActivity activity = (HomeActivity) ClientManager.getInstance().getCurrentContext(); // ou une référence déjà existante
        if (activity == null) return;

        MatchMakingFragment fragment = (MatchMakingFragment) activity.getSupportFragmentManager()
            .findFragmentByTag("MATCHMAKING_FRAGMENT");

        if (fragment != null) {
            fragment.show(); // Affiche l'overlay et démarre le timer
            Log.e("For The Oil", "MatchMakingFragment found!");
        } else {
            Log.e("For The Oil", "MatchMakingFragment not found!");
        }
    }


    public void leaveMatchmaking(){
        Log.d("For The Oil","Server asked us to leave the matchmaking !");
    }







}
