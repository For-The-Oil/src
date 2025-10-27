package io.github.server.network.kryolistener;

import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import io.github.server.engine.manager.ClientManager;
import io.github.shared.local.data.EnumsTypes.KryoMessageType;
import io.github.shared.local.data.network.KryoMessage;
import io.github.shared.local.data.requests.AuthRequest;

/**
 * Listener that manage the AuthRequest types.
 *
 * Cases like :
 * - Login
 * - Register
 */
public class AuthListener extends Listener {

    @Override
    public void connected(Connection connection) {
        System.out.println("New client connected : " + connection.getRemoteAddressTCP());
    }

    @Override
    public void disconnected(Connection connection) {
        System.out.println("Client disconnected : " + connection.getRemoteAddressTCP());
        // Supprimer le client de la liste auth si présent
        //ServerNetwork.getInstance().getAuthClientNetworkList().removeIf(c -> c.getConnection() == connection);
    }

    @Override
    public void received(Connection connection, Object object) {
        if(object instanceof KryoMessage){

            KryoMessage kryoMessage = (KryoMessage) object;

            if (kryoMessage.getType()==KryoMessageType.AUTH||kryoMessage.getObj() instanceof AuthRequest) {

                AuthRequest request = (AuthRequest) kryoMessage.getObj();

                // Gestion cas par cas de la demande de connexion
                switch (request.getMode()) {
                    case REGISTER:
                        ClientManager.getInstance().registerClient(connection, request);
                        break;

                    case LOGIN:
                        ClientManager.getInstance().loginClient(connection, request);
                        break;

                    case TOKEN:
                        ClientManager.getInstance().loginByTokenClient(connection, request);
                        break;
                }
            }
        }
    }
}
