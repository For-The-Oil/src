package io.github.core.client_engine.kryolistener;

import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;

import io.github.core.client_engine.manager.KryoClientManager;
import io.github.shared.local.data.network.KryoMessage;
import io.github.shared.local.data.requests.AuthRequest;
import io.github.shared.local.data.EnumsTypes.KryoMessageType;


/**
 * <h1>ClientAuthListener</h1>
 *
 * <p>
 * Listener KryoNet côté client chargé de recevoir les messages du serveur
 * et de déléguer la gestion des messages d'authentification au
 * {@link KryoClientManager}.
 * </p>
 *
 * <p>
 * Ce listener s'occupe de trois événements principaux :
 * </p>
 * <ul>
 *     <li>{@link #connected(Connection)} : appelé lorsque le client se connecte au serveur.</li>
 *     <li>{@link #disconnected(Connection)} : appelé lorsque le client se déconnecte du serveur.</li>
 *     <li>{@link #received(Connection, Object)} : appelé lorsqu'un message est reçu du serveur.</li>
 * </ul>
 *
 * <p>
 * Les messages de type {@link KryoMessageType#AUTH} contenant un
 * {@link AuthRequest} sont automatiquement transmis au
 * {@link KryoClientManager} via {@link KryoClientManager#handleAuthResponse(AuthRequest)}.
 * Les autres types de messages sont affichés dans la console pour débogage.
 * </p>
 *
 * <p>
 * Exemple d'utilisation :
 * </p>
 * <pre>
 * KryoClientManager manager = KryoClientManager.getInstance();
 * client.addListener(new ClientAuthListener(manager));
 * </pre>
 *
 * @see KryoClientManager
 * @see KryoMessage
 * @see AuthRequest
 * @see KryoMessageType
 */
public class ClientAuthListener extends Listener {

    private final KryoClientManager manager;

    /**
     * Crée un listener pour déléguer les messages d'authentification au manager.
     *
     * @param manager l'instance de {@link KryoClientManager} responsable du traitement
     */
    public ClientAuthListener(KryoClientManager manager) {
        this.manager = manager;
    }

    /**
     * Appelé lorsque le client se connecte au serveur.
     *
     * @param connection la connexion TCP établie
     */
    @Override
    public void connected(Connection connection) {
        System.out.println("Client connected to server: " + connection.getRemoteAddressTCP());
        manager.setConnected(true);
    }

    /**
     * Appelé lorsque le client se déconnecte du serveur.
     *
     * @param connection la connexion TCP qui a été fermée
     */
    @Override
    public void disconnected(Connection connection) {
        System.out.println("Client disconnected from server.");
        manager.setConnected(false);
    }

    /**
     * Appelé lorsqu'un message est reçu depuis le serveur.
     *
     * <p>
     * Si le message est de type AUTH et contient un {@link AuthRequest},
     * il est transmis au {@link KryoClientManager} pour traitement.
     * Sinon, le message est affiché dans la console pour débogage.
     * </p>
     *
     * @param connection la connexion TCP ayant reçu le message
     * @param object le message reçu du serveur
     */
    @Override
    public void received(Connection connection, Object object) {
        if (!(object instanceof KryoMessage)) return;

        KryoMessage message = (KryoMessage) object;

        if (message.getType() == KryoMessageType.AUTH && message.getObj() instanceof AuthRequest) {
            // Délègue le traitement au manager
            manager.handleAuthResponse((AuthRequest) message.getObj());
        } else {
            System.out.println("Received message of type " + message.getType() + ": " + message.getObj());
        }
    }
}
