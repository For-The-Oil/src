package io.github.android.manager;

import android.util.Log;

import java.util.HashMap;

import io.github.core.client_engine.factory.KryoMessagePackager;
import io.github.shared.local.data.EnumsTypes.GameModeType;
import io.github.shared.local.data.EnumsTypes.MatchModeType;
import io.github.shared.local.data.network.KryoMessage;
import io.github.shared.local.data.requests.MatchMakingRequest;

public class MatchMakingManager {

    public static MatchMakingManager INSTANCE;

    private GameModeType gameMode;

    private MatchMakingManager(){}

    public static  MatchMakingManager getInstance(){
        if(INSTANCE==null) INSTANCE = new MatchMakingManager();
        return INSTANCE;
    }

    public void askMatchmaking(GameModeType gameMode){
        this.gameMode = gameMode;
        HashMap<String,String> myMap = new HashMap<>();
        MatchMakingRequest myRequest = new MatchMakingRequest(MatchModeType.ASK, gameMode, myMap);
        KryoMessage message = KryoMessagePackager.packAuthRequest(myRequest, SessionManager.getInstance().getToken());
        ClientManager.getInstance().getKryoManager().send(message);
        Log.d("For The Oil","Token is :"+SessionManager.getInstance().getToken());
        Log.d("For The Oil","Asking Matchmaking KryoMessage Sent !"+gameMode.toString());
    }

    public void cancelMatchmaking(){
        HashMap<String,String> myMap = new HashMap<>();
        MatchMakingRequest myRequest = new MatchMakingRequest(MatchModeType.CANCEL, gameMode, myMap);
        KryoMessage message = KryoMessagePackager.packAuthRequest(myRequest, SessionManager.getInstance().getToken());
        ClientManager.getInstance().getKryoManager().send(message);
        Log.d("For The Oil","Token is :"+SessionManager.getInstance().getToken());
        Log.d("For The Oil","Cancel Matchmaking KryoMessage Sent !"+gameMode.toString());
    }







}
