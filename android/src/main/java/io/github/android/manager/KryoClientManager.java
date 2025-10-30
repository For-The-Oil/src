package io.github.android.manager;

import android.util.Log;
import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;

import io.github.shared.local.data.network.KryoRegistry;
import io.github.shared.local.data.requests.AuthRequest;
import io.github.shared.local.data.EnumsTypes.KryoMessageType;
import io.github.shared.local.data.network.KryoMessage;

public class KryoClientManager {

    private static final String TAG = "KryoClientManager";

    private final Client client;
    private boolean connected = false;

    public interface ConnectionListener {
        void onConnected();
        void onDisconnected();
        void onMessageReceived(Object message);
    }

    private ConnectionListener connectionListener;

    public KryoClientManager() {
        client = new Client();

        // Enregistrer toutes les classes Kryo
        KryoRegistry.registerAll(client.getKryo());
        Log.d(TAG, "Kryo classes registered");

        // Démarrer le client
        client.start();
        Log.d(TAG, "KryoNet client started");

        // Ajouter un listener pour debug + réception messages
        client.addListener(new Listener() {
            @Override
            public void connected(Connection connection) {
                connected = true;
                Log.d(TAG, "Connected to server: " + connection.getRemoteAddressTCP());
                if (connectionListener != null) connectionListener.onConnected();
            }

            @Override
            public void disconnected(Connection connection) {
                connected = false;
                Log.d(TAG, "Disconnected from server");
                if (connectionListener != null) connectionListener.onDisconnected();
            }

            @Override
            public void received(Connection connection, Object object) {
                Log.d(TAG, "Received object: " + object);
                if (connectionListener != null) connectionListener.onMessageReceived(object);
            }
        });
    }

    public void setConnectionListener(ConnectionListener listener) {
        this.connectionListener = listener;
    }

    public boolean isConnected() {
        return connected && client.isConnected();
    }

    public void connect(String host, int port) {
        new Thread(() -> {
            try {
                Log.d(TAG, "Trying to connect to " + host + ":" + port);
                client.connect(5000, host, port); // timeout 5s
                Log.d(TAG, "Connection attempt finished");
            } catch (Exception e) {
                Log.e(TAG, "Connection failed", e);
            }
        }, "KryoNet-ConnectThread").start();
    }

    public void disconnect() {
        client.close();
        Log.d(TAG, "Client closed");
        connected = false;
    }

    public void sendMessage(Object msg) {
        if (!isConnected()) {
            Log.w(TAG, "Cannot send message, client not connected");
            return;
        }
        client.sendTCP(msg);
        Log.d(TAG, "Message sent: " + msg);
    }

    public void sendLogin(AuthRequest loginRequest) {
        Log.d(TAG, "Preparing login request: " + loginRequest.getKeys());
        sendMessage(new KryoMessage(KryoMessageType.AUTH, null, loginRequest));
    }

    public void sendRegister(AuthRequest registerRequest) {
        Log.d(TAG, "Preparing register request: " + registerRequest.getKeys());
        sendMessage(new KryoMessage(KryoMessageType.AUTH, null, registerRequest));
    }
}
