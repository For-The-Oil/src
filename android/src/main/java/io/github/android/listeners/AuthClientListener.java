package io.github.android.listeners;

import android.util.Log;

import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;

import io.github.android.manager.ClientManager;
import io.github.shared.local.data.network.KryoMessage;
import io.github.shared.local.data.requests.AuthRequest;

/**
 * <h1>AuthClientListener</h1>
 * Listener réseau spécialisé pour la gestion des messages d'authentification
 * entre le client Android et le serveur via KryoNet.
 *
 * <p>Cette classe intercepte les événements de connexion, de déconnexion et
 * les messages reçus. Elle ne traite que les {@link KryoMessage} contenant
 * un {@link AuthRequest}, et délègue la logique métier au {@link ClientManager}.</p>
 *
 * <p>En cas d'échec d'authentification (REGISTER_FAIL, LOGIN_FAIL, TOKEN_FAIL),
 * la connexion est fermée et le {@link ClientManager} est notifié.
 * En cas de succès (REGISTER_SUCCESS, LOGIN_SUCCESS, TOKEN_SUCCESS),
 * le {@link ClientManager} est également notifié pour mettre à jour l'état
 * de l'application.</p>
 */
public class AuthClientListener extends Listener {

    /** Tag utilisé pour identifier les logs dans Logcat. */
    private static final String TAG = "AuthClientListener";

    /**
     * Callback appelé lorsque le client établit une connexion avec le serveur.
     *
     * @param connection la connexion nouvellement établie
     */
    @Override
    public void connected(Connection connection) {
        Log.d(TAG, "Connected to server!");
    }

    /**
     * Callback appelé lorsque la connexion est fermée (par le client ou le serveur).
     *
     * @param connection la connexion qui vient d'être fermée
     */
    @Override
    public void disconnected(Connection connection) {
        Log.d(TAG, "Disconnected from server.");
    }

    /**
     * Callback appelé lorsqu'un objet est reçu depuis le serveur.
     *
     * <p>Seuls les {@link KryoMessage} contenant un {@link AuthRequest}
     * sont traités. Les autres objets sont ignorés.</p>
     *
     * <p>Selon le mode d'authentification renvoyé par le serveur,
     * cette méthode :
     * <ul>
     *   <li>Ferme la connexion et notifie le {@link ClientManager} en cas d'échec
     *       (REGISTER_FAIL, LOGIN_FAIL, TOKEN_FAIL).</li>
     *   <li>Notifie le {@link ClientManager} en cas de succès
     *       (REGISTER_SUCCESS, LOGIN_SUCCESS, TOKEN_SUCCESS).</li>
     * </ul>
     * </p>
     *
     * @param connection la connexion par laquelle l'objet a été reçu
     * @param object     l'objet reçu (peut être de tout type, filtré ici)
     */
    @Override
    public void received(Connection connection, Object object) {
        if (!(object instanceof KryoMessage)) {
            return;
        }

        KryoMessage msg = (KryoMessage) object;

        if (!(msg.getObj() instanceof AuthRequest)) {
            return;
        }

        AuthRequest myRequest = (AuthRequest) msg.getObj();
        ClientManager manager = ClientManager.getInstance();

        Log.d(TAG, "Server response: " + msg.getType());
        Log.d(TAG, "Server response: " + msg.getObj());

        switch (myRequest.getMode()) {
            case REGISTER_FAIL:
                Log.w(TAG, "Registration refused by server, closing connection...");
                connection.close();
                manager.registerFailure(myRequest);
                break;

            case LOGIN_FAIL:
                Log.w(TAG, "Login refused by server, closing connection...");
                connection.close();
                manager.loginFailure(myRequest);
                break;

            case TOKEN_FAIL:
                Log.w(TAG, "TOKEN Login refused by server, closing connection...");
                connection.close();
                manager.tokenFailure(myRequest);
                break;

            case REGISTER_SUCCESS:
                Log.i(TAG, "Register success !");
                manager.registerSuccess(myRequest);
                break;

            case LOGIN_SUCCESS:
                Log.i(TAG, "Login success !");
                manager.loginSuccess(myRequest);
                break;

            case TOKEN_SUCCESS:
                Log.i(TAG, "Token success !");
                manager.tokenSuccess(myRequest);
                break;
        }
    }
}

