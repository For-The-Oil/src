package io.github.server.server_engine.utils;

import com.esotericsoftware.kryonet.Connection;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

import io.github.server.data.ServerGame;
import io.github.server.data.network.ServerNetwork;
import io.github.shared.data.EnumsTypes.GameModeType;
import io.github.shared.data.network.ClientNetwork;
import io.github.shared.data.network.Player;

public final class PlayerChecker {


    public static ClientNetwork isValidToken(Connection connection, String token) {
        // Récupère la liste des clients connectés/authentifiés
        ArrayList<ClientNetwork> clients = ServerNetwork.getInstance().getAuthClientNetworkList();

        for (ClientNetwork client : clients) {
            // Vérifie que la connection correspond
            if (client.getConnection().equals(connection)) {
                // Vérifie que le token correspond
                if (client.getToken() != null && client.getToken().equals(token)) {
                    return client;
                }
                return null; // connection ok mais token invalide
            }
        }
        // pas trouvé => client non connecté
        return null;
    }


    public static ServerGame getGameOfClient(ClientNetwork client) {
        if (client == null || client.getUuid() == null) return null;

        UUID targetUUID = client.getUuid();
        HashMap<GameModeType, ArrayList<ServerGame>> gameModeArrayListOfGames =  ServerNetwork.getInstance().getGameModeArrayListOfGames();
        synchronized (gameModeArrayListOfGames) {
            for (ArrayList<ServerGame> games : gameModeArrayListOfGames.values()) {
                for (ServerGame game : games) {
                    for (Player player : game.getPlayersList()) {
                        if (player.getUuid().equals(targetUUID)) {
                            return game;
                        }
                    }
                }
            }
        }
        return null; // pas trouvé
    }



}
