package io.github.core.client_engine.manager;

import com.esotericsoftware.kryonet.Client;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import io.github.core.client_engine.kryolistener.ClientAuthListener;
import io.github.shared.local.data.EnumsTypes.AuthModeType;
import io.github.shared.local.data.EnumsTypes.KryoMessageType;
import io.github.shared.local.data.network.KryoMessage;
import io.github.shared.local.data.network.KryoRegistry;
import io.github.shared.local.data.requests.AuthRequest;
import io.github.shared.local.data.gameobject.Deck;
public class KryoClientManager {

    private static KryoClientManager instance;

    private final Client client;
    private boolean connected = false;
    private String savedToken;
    private Map<String, Deck> decks;
    private String username;
    private Runnable onConnectedCallback;

    private AuthEventListener authListener;

    public void setConnected(boolean b) {
        this.connected = b;
    }

    public interface AuthEventListener {
        void onLoginSuccess(String username, Map<String, Deck> decks, String token);
        void onLoginFailure(String message);
        void onRegisterSuccess(String username, Map<String, Deck> decks, String token);
        void onRegisterFailure(String message);
    }

    public void setConnectionCallback(Runnable callback) {
        this.onConnectedCallback = callback;
    }

    private KryoClientManager() {
        client = new Client();

        KryoRegistry.registerAll(client.getKryo());

        client.addListener(new ClientAuthListener(this));

        client.start();
    }

    public static KryoClientManager getInstance() {
        if (instance == null) instance = new KryoClientManager();
        return instance;
    }

    public void setAuthEventListener(AuthEventListener listener) {
        this.authListener = listener;
    }

    public boolean isConnected() {
        return connected;
    }

    public void connect(String host, int port) {
        new Thread(() -> {
            try {
                client.connect(5000, host, port);
                connected = true;
                System.out.println("Connected to server!");

                // Appeler le callback si défini
                if (onConnectedCallback != null) {
                    onConnectedCallback.run();
                    onConnectedCallback = null;
                }
            } catch (IOException e) {
                e.printStackTrace();
                connected = false;
            }
        }).start();
    }

    // ----- LOGIN / REGISTER -----
    public void sendLogin(String email, String password) {
        if (!connected) return;
        HashMap<String, String> keys = new HashMap<>();
        keys.put("email", email);
        keys.put("password", password);
        AuthRequest request = new AuthRequest(AuthModeType.LOGIN, keys);
        client.sendTCP(new KryoMessage(KryoMessageType.AUTH, null, request));
    }

    public void sendRegister(String email, String username, String password, String password2) {
        if (!connected) return;
        HashMap<String, String> keys = new HashMap<>();
        keys.put("email", email);
        keys.put("username", username);
        keys.put("password", password);
        keys.put("password2", password2);
        AuthRequest request = new AuthRequest(AuthModeType.REGISTER, keys);
        client.sendTCP(new KryoMessage(KryoMessageType.AUTH, null, request));
    }

    public void sendLoginWithToken(String token) {
        if (!connected || token == null) return;
        HashMap<String, String> keys = new HashMap<>();
        keys.put("token", token);
        AuthRequest request = new AuthRequest(AuthModeType.LOGIN, keys);
        client.sendTCP(new KryoMessage(KryoMessageType.AUTH, null, request));
    }

    // ----- RESPONSE HANDLER -----
    public void handleAuthResponse(AuthRequest authRequest) {
        HashMap<String, String> data = authRequest.getKeys();
        boolean success = Boolean.parseBoolean(data.getOrDefault("success", "false"));
        String message = data.getOrDefault("message", "");

        ObjectMapper mapper = new ObjectMapper();
        Map<String, Deck> parsedDecks = null;

        if (success) {
            username = data.get("username");
            savedToken = data.get("token");
            String decksJson = data.getOrDefault("deck", "{}");

            try {
                parsedDecks = mapper.readValue(decksJson, new TypeReference<Map<String, Deck>>() {});
            } catch (Exception e) {
                e.printStackTrace();
                parsedDecks = new HashMap<>();
            }
            decks = parsedDecks;

            if (authListener != null) {
                switch (authRequest.getMode()) {
                    case LOGIN_SUCCESS: authListener.onLoginSuccess(username, decks, savedToken); break;
                    case REGISTER_SUCCESS: authListener.onRegisterSuccess(username, decks, savedToken);break;
                    default: System.out.println("Unknown success mode: " + authRequest.getMode());break;
                }
            }

        } else {
            if (authListener != null) {
                switch (authRequest.getMode()) {
                    case LOGIN_FAIL : authListener.onLoginFailure(message);break;
                    case REGISTER_FAIL : authListener.onRegisterFailure(message);break;
                    default: System.out.println("Unknown failure mode: " + authRequest.getMode());break;
                }
            }
        }
    }


    // ----- GETTERS -----
    public Map<String, Deck> getDecks() {
        return decks;
    }

    public String getUsername() {
        return username;
    }

    public String getSavedToken() {
        return savedToken;
    }
}
