package io.github.core.client_engine.kryolistener;

import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;

import io.github.shared.local.data.network.KryoMessage;
import io.github.shared.local.data.requests.AuthRequest;
import io.github.shared.local.data.network.ClientNetwork;

/**
 * Listener KryoNet côté client pour gérer l'authentification.
 * Reçoit les messages du serveur et met à jour l'état client.
 */
public class ClientAuthListener extends Listener {

    private ClientNetwork myClientNetwork; // Informations du client après connexion
    private boolean isAuthenticated = false; // Flag pour savoir si connecté

    @Override
    public void received(Connection connection, Object object) {
        if (!(object instanceof KryoMessage)) return;

        KryoMessage msg = (KryoMessage) object;

        // On ne traite que les messages Auth
        if (msg.getType() != null && msg.getType().name().equals("AUTH")) {
            if (msg.getObj() instanceof AuthRequest) {
                AuthRequest authRequest = (AuthRequest) msg.getObj();
                //handleAuthResponse(authRequest);
            }
        }
    }

    /**
     * Traite la réponse du serveur pour l'authentification.
     */
    private void handleAuthResponse(Connection connection, Object object) {

        if(object instanceof KryoMessage){

            KryoMessage kryomessage = (KryoMessage) object;





        }
    }

    // Getters pour savoir si le client est connecté et récupérer ses infos
    public boolean isAuthenticated() {
        return isAuthenticated;
    }

    public ClientNetwork getClientNetwork() {
        return myClientNetwork;
    }
}
