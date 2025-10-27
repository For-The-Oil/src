package io.github.server.network.kryolistener;

import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;

import io.github.server.data.network.ServerNetwork;
import io.github.shared.local.data.network.ClientNetwork;
import io.github.shared.local.data.requests.AuthRequest;

public class AuthListener extends Listener {

    @Override
    public void connected(Connection connection) {
        System.out.println("New client connected : " + connection.getRemoteAddressTCP());
    }

    @Override
    public void disconnected(Connection connection) {
        System.out.println("Client déconnecté : " + connection.getRemoteAddressTCP());
        // Supprimer le client de la liste auth si présent
        //ServerNetwork.getInstance().getAuthClientNetworkList().removeIf(c -> c.getConnection() == connection);
    }

    @Override
    public void received(Connection connection, Object object) {
        if (object instanceof AuthRequest) {

            AuthRequest request = (AuthRequest) object;

            // Gestion cas par cas de la demande de connexion
            switch (request.getMode()) {
                case REGISTER:
                    registerClient(connection, request);
                    break;

                case LOGIN:
                    loginClient(connection, request);
                    break;

                case TOKEN:
                    loginByTokenClient(connection, request);
                    break;
            }

        }
    }

    public void loginClient(Connection connection, AuthRequest object){
        System.out.println("Demande de login");
    }

    public void loginByTokenClient(Connection connection, AuthRequest object){
        System.out.println("Demande de login par token");
    }

    public void registerClient(Connection connection, AuthRequest object){
        System.out.println("Demande de register");
    }



    // Méthode fictive pour vérification des identifiants
    private boolean checkCredentials() {
        return true;
    }

}
