package io.github.android.manager;

import android.util.Log;
import io.github.shared.local.data.requests.AuthRequest;

public class ClientManager {

    private static final String TAG = "ClientManager";
    private static ClientManager INSTANCE;
    private KryoClientManager kryoManager;

    private ClientManager() {
        kryoManager = new KryoClientManager();
        Log.d(TAG, "ClientManager initialized");
    }

    public static ClientManager getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new ClientManager();
        }
        return INSTANCE;
    }

    public void setConnectionListener(KryoClientManager.ConnectionListener listener) {
        kryoManager.setConnectionListener(listener);
    }

    public void connectToServer(String host, int port) {
        Log.d(TAG, "Connecting to server " + host + ":" + port);
        kryoManager.connect(host, port);
    }

    public void disconnect() {
        Log.d(TAG, "Disconnecting from server");
        kryoManager.disconnect();
    }

    public void login(String email, String password) {
        Log.d(TAG, "Login requested for " + email);
        AuthRequest login = new AuthRequest(); // initialise avec ton email/password
        login.setMode(io.github.shared.local.data.EnumsTypes.AuthModeType.LOGIN);
        login.getKeys().put("email", email);
        login.getKeys().put("password", password);
        kryoManager.sendLogin(login);
    }

    public void register(String email, String username, String password) {
        Log.d(TAG, "Register requested for " + email);
        AuthRequest register = new AuthRequest();
        register.setMode(io.github.shared.local.data.EnumsTypes.AuthModeType.REGISTER);
        register.getKeys().put("email", email);
        register.getKeys().put("username", username);
        register.getKeys().put("password", password);
        kryoManager.sendRegister(register);
    }

    public boolean isConnected() {
        return kryoManager.isConnected();
    }

    public KryoClientManager getKryoManager() {
        return this.kryoManager;
    }
}
