package io.github.server.data.network;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import io.github.server.data.ServerGame;
import io.github.server.game_engine.GameLauncher;
import io.github.shared.data.EnumsTypes.GameModeType;
import io.github.shared.data.network.ClientNetwork;
import com.esotericsoftware.kryonet.Connection;
public class ServerNetwork {
    private static final ServerNetwork INSTANCE = new ServerNetwork();
    private final long start_timestamp;
    private HashMap<GameModeType, ArrayList<ClientNetwork>> matchmakingMap;
    private ArrayList<ClientNetwork> authClientNetworkList;
    private ConcurrentHashMap<UUID, GameLauncher> gameMapByUUID;
    private HashMap<GameModeType, ArrayList<ServerGame>> gameModeArrayListOfGames;

    public static ServerNetwork getInstance() {
        return INSTANCE;
    }

    private ServerNetwork() {
        this.gameMapByUUID = new ConcurrentHashMap<>();
        this.gameModeArrayListOfGames = new HashMap<>();
        start_timestamp = System.currentTimeMillis();
        matchmakingMap = new HashMap<>();
        authClientNetworkList = new ArrayList<>();
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

    public ClientNetwork getClientByConnection(Connection connection) {
        if (connection == null) return null;

        synchronized (authClientNetworkList) {
            return authClientNetworkList.stream()
                .filter(c -> c.getConnection() != null && c.getConnection().equals(connection))
                .findFirst()
                .orElse(null);
        }
    }

    public ClientNetwork getClientByUUID(UUID uuid) {
        if (uuid == null) return null;

        synchronized (authClientNetworkList) {
            return authClientNetworkList.stream()
                .filter(c -> uuid.equals(c.getUuid()))
                .findFirst()
                .orElse(null);
        }
    }

    public ClientNetwork getClientByToken(String token) {
        if (token == null) return null;

        synchronized (authClientNetworkList) {
            return authClientNetworkList.stream()
                .filter(c -> token.equals(c.getToken()))
                .findFirst()
                .orElse(null);
        }
    }

    public ClientNetwork getClientByUsername(String username) {
        if (username == null) return null;

        synchronized (authClientNetworkList) {
            return authClientNetworkList.stream()
                .filter(c -> username.equals(c.getUsername()))
                .findFirst()
                .orElse(null);
        }
    }


    public ClientNetwork getActiveClientByUUID(UUID uuid) {
        synchronized (authClientNetworkList) {
            return authClientNetworkList.stream()
                .filter(c -> c.getUuid().equals(uuid) && c.getConnection() != null && c.getConnection().isConnected())
                .findFirst()
                .orElse(null);
        }
    }

    public HashMap<GameModeType, ArrayList<ServerGame>> getGameModeArrayListOfGames() {
        return gameModeArrayListOfGames;
    }

    public ConcurrentHashMap<UUID, GameLauncher> getGameMapByUUID() {
        return gameMapByUUID;
    }
}
