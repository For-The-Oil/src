package io.github.server.data.network;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

import io.github.server.data.Game;
import io.github.shared.local.data.EnumsTypes.GameModeType;
import io.github.shared.local.data.network.ClientNetwork;

public class ServerNetwork {
    private static final ServerNetwork INSTANCE = new ServerNetwork();
    private final long start_timestamp;
    private HashMap<GameModeType, ArrayList<ClientNetwork>> matchmakingMap;
    private ArrayList<ClientNetwork> authClientNetworkList;
    private ConcurrentHashMap<Thread , Game> gameMap;

    public static ServerNetwork getInstance() {
        return INSTANCE;
    }

    private ServerNetwork() {
        start_timestamp = System.currentTimeMillis();
        matchmakingMap = new HashMap<>();
        authClientNetworkList = new ArrayList<>();
        gameMap = new ConcurrentHashMap<>();
    }


    public long getStart_timestamp() {
        return start_timestamp;
    }


    public HashMap<GameModeType, ArrayList<ClientNetwork>> getMatchmakingMap() {
        return matchmakingMap;
    }

    public ArrayList<ClientNetwork> getAuthClientNetworkList() {
        return authClientNetworkList;
    }

    public ConcurrentHashMap<Thread, Game> getGameMap() {
        return gameMap;
    }
}
