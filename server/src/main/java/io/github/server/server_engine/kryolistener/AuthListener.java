package io.github.server.server_engine.kryolistener;

import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;

import io.github.server.data.network.ServerNetwork;
import io.github.server.server_engine.manager.ClientAuthManager;
import io.github.shared.local.data.EnumsTypes.KryoMessageType;
import io.github.shared.local.data.network.ClientNetwork;
import io.github.shared.local.data.network.KryoMessage;
import io.github.shared.local.data.requests.AuthRequest;

/**
 * AuthListener
 *
 * This class is a specialized KryoNet {@link Listener} responsible for handling
 * all incoming authentication-related requests from clients.
 *
 * <p>It acts as a bridge between the network layer (KryoNet)
 * and the server-side authentication logic implemented in {@link ClientAuthManager}.
 *
 * <h2>Responsibilities:</h2>
 * <ul>
 *     <li>Detect when a client connects or disconnects from the server.</li>
 *     <li>Intercept and process incoming {@link KryoMessage} objects.</li>
 *     <li>Handle authentication-related requests via {@link AuthRequest}:
 *         <ul>
 *             <li><b>REGISTER</b> → Create a new user account.</li>
 *             <li><b>LOGIN</b> → Validate user credentials and establish a session.</li>
 *             <li><b>TOKEN</b> → Reconnect using a saved authentication token.</li>
 *         </ul>
 *     </li>
 * </ul>
 *
 * <p>This listener ensures that only properly formatted and typed network messages
 * are forwarded to the correct logic handler.</p>
 *
 * @author
 *      Gauthier — Server-side Network & Authentication Logic
 * @version
 *      1.0 — Initial implementation
 */
public class AuthListener extends Listener {

    /**
     * Called when a new client successfully connects to the server.
     *
     * <p>This method is invoked automatically by KryoNet.
     * It can be used to perform handshake initialization,
     * logging, or queue preparation for the new connection.</p>
     *
     * @param connection The client connection instance created by KryoNet.
     */
    @Override
    public void connected(Connection connection) {
        System.out.println("New client connected: " + connection.getRemoteAddressTCP());

        // TODO : Ajouter une vérification BAN-IP

    }

    /**
     * Called when a client disconnects from the server.
     *
     * <p>This method is triggered automatically when a network connection
     * is closed, either voluntarily (client logout) or involuntarily (timeout, crash, etc.).</p>
     *
     * <p>In future implementations, this can be used to clean up sessions,
     * remove the client from the authenticated list, or persist disconnection timestamps.</p>
     *
     * @param connection The disconnected client connection.
     */
    @Override
    public void disconnected(Connection connection) {
        int id = connection.getID();

        // Cherche le client correspondant
        ClientNetwork disconnectedClient = ServerNetwork.getInstance()
            .getAuthClientNetworkList()
            .stream()
            .filter(c -> c.getConnection() != null && c.getConnection().getID() == id)
            .findFirst()
            .orElse(null);

        if (disconnectedClient != null) {
            String ip = disconnectedClient.getIp();
            if (ip != null) {
                System.out.println("Client disconnected: " + ip);
            } else {
                System.out.println("Client disconnected, id=" + id);
            }

            // Supprime le client de la liste
            ServerNetwork.getInstance().getAuthClientNetworkList().remove(disconnectedClient);

            // Supprime également le client de toutes les listes matchmaking
            ServerNetwork.getInstance().getMatchmakingMap().forEach((mode, clients) -> {
                if (clients.remove(disconnectedClient)) {

                    System.out.println("Removed client from matchmaking for mode: " + mode);
                }
            });
        }

        System.out.println("Remaining auth clients: " +
            ServerNetwork.getInstance().getAuthClientNetworkList().size());
    }


    /**
     * Called whenever the server receives a new object from a connected client.
     *
     * <p>This is the core of the listener. It filters incoming objects,
     * checks their type, and delegates authentication-related logic
     * to the {@link ClientAuthManager} singleton.</p>
     *
     * <h3>Processing Flow:</h3>
     * <ol>
     *     <li>Verify that the received object is an instance of {@link KryoMessage}.</li>
     *     <li>Check if the message type equals {@link KryoMessageType#AUTH}.</li>
     *     <li>Extract the {@link AuthRequest} payload.</li>
     *     <li>Call the appropriate {@link ClientAuthManager} method depending on the request mode:
     *         <ul>
     *             <li>{@code REGISTER} → register a new user</li>
     *             <li>{@code LOGIN} → authenticate existing user</li>
     *             <li>{@code TOKEN} → re-login via saved session token</li>
     *         </ul>
     *     </li>
     * </ol>
     *
     * @param connection The source client connection.
     * @param object The object received from the client (usually a {@link KryoMessage}).
     */
    @Override
    public void received(Connection connection, Object object) {

        //Ensure the received object is a KryoMessage
        if (object instanceof KryoMessage) {

            KryoMessage kryoMessage = (KryoMessage) object;

            //Check if it’s an authentication-related message
            if (kryoMessage.getType() == KryoMessageType.AUTH || kryoMessage.getObj() instanceof AuthRequest) {

                AuthRequest request = (AuthRequest) kryoMessage.getObj();

                //Handle authentication mode appropriately
                switch (request.getMode()) {
                    case REGISTER:
                        System.out.println("Received REGISTER request from " + connection.getRemoteAddressTCP());
                        ClientAuthManager.getInstance().registerClient(connection, request);
                        break;

                    case LOGIN:
                        System.out.println("Received LOGIN request from " + connection.getRemoteAddressTCP());
                        ClientAuthManager.getInstance().loginClient(connection, request);
                        break;

                    case TOKEN:
                        System.out.println("Received TOKEN login request from " + connection.getRemoteAddressTCP());
                        ClientAuthManager.getInstance().loginByTokenClient(connection, request);
                        break;

                    default:
                        System.err.println("Unknown AuthRequest mode received: " + request.getMode());
                        break;
                }
            }
        }
    }
}
