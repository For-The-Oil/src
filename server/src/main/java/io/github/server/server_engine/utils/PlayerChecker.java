package io.github.server.server_engine.utils;

import com.esotericsoftware.kryonet.Connection;

import java.util.ArrayList;

import io.github.server.data.network.ServerNetwork;
import io.github.server.server_engine.manager.ClientAuthManager;
import io.github.shared.local.data.network.ClientNetwork;

public final class PlayerChecker {


    public static boolean isValidToken(Connection connection, String token) {
        // Récupère la liste des clients connectés/authentifiés
        ArrayList<ClientNetwork> clients = ServerNetwork.getInstance().getAuthClientNetworkList();

        for (ClientNetwork client : clients) {
            // Vérifie que la connection correspond
            if (client.getConnection().equals(connection)) {
                // Vérifie que le token correspond
                if (client.getToken() != null && client.getToken().equals(token)) {
                    return true;
                }
                return false; // connection ok mais token invalide
            }
        }
        // pas trouvé => client non connecté
        return false;
    }








}
