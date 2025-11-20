package io.github.android.manager;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import io.github.android.activity.SplashActivity;
import io.github.android.activity.HomeActivity;
import io.github.android.gui.fragment.launcher.LoginFragment;
import io.github.android.gui.fragment.launcher.RegisterFragment;
import io.github.android.gui.fragment.launcher.ServerFragment;
import io.github.core.client_engine.factory.KryoMessagePackager;
import io.github.core.client_engine.factory.RequestFactory;
import io.github.core.client_engine.manager.KryoClientManager;
import io.github.shared.data.network.KryoMessage;
import io.github.shared.data.requests.AuthRequest;

/**
 * Manager that manage the connection of the client
 */
public class ClientManager {
    private static ClientManager INSTANCE;
    private Activity currentContext;
    private int port;
    private String ip;
    private final KryoClientManager kryoManager;

    private ClientManager() {
        kryoManager = new KryoClientManager();
    }

    public static  ClientManager getInstance() {
        if (INSTANCE == null) INSTANCE = new ClientManager();
        return INSTANCE;
    }

    public void login(String email, String password) {
        AuthRequest request = RequestFactory.createLoginRequest(email, password);
        KryoMessage message = KryoMessagePackager.packAuthRequest(request);
        kryoManager.send(message);
        Log.d("For The Oil","Login KryoMessage Sent !");
    }

    public void register(String email, String username, String password, String password2) {
        AuthRequest request = RequestFactory.createRegisterRequest(email, username, password, password2);
        KryoMessage message = KryoMessagePackager.packAuthRequest(request);
        kryoManager.send(message);
        Log.d("For The Oil","Register KryoMessage Sent !");
    }


    //---------
    // Connection SUCCESS / FAILURE
    //---------

    public void loginSuccess(AuthRequest myRequest){
        Log.d("Auth","Login Sucess !!!!!!");
        buildSession(myRequest);
        launchSecondActivityAndPassBySplashScreen();
    }

    public void registerSuccess(AuthRequest myRequest){
        buildSession(myRequest);
        launchSecondActivityAndPassBySplashScreen();
    }

    public void tokenSuccess(AuthRequest myRequest){
        buildSession(myRequest);
        launchSecondActivityAndPassBySplashScreen();
    }

    public void loginFailure(AuthRequest myRequest) {
        String errorMessage = myRequest.getKeys().get("message");
        Activity activity = (Activity) currentContext;
        activity.runOnUiThread(() -> {
            LoginFragment fragment =
                (LoginFragment) ((AppCompatActivity) activity).getSupportFragmentManager().findFragmentByTag("f1");
            if (fragment != null) {
                fragment.showError(errorMessage != null ? errorMessage : "Error !");
            }
        });
    }


    public void registerFailure(AuthRequest myRequest) {
        String errorMessage = myRequest.getKeys().get("message");
        Activity activity = (Activity) currentContext;
        activity.runOnUiThread(() -> {
            RegisterFragment fragment =
                (RegisterFragment) ((AppCompatActivity) activity).getSupportFragmentManager().findFragmentByTag("f2");
            if (fragment != null) {
                fragment.showError(errorMessage != null ? errorMessage : "Error !");
            }
        });
    }

    public void tokenFailure(AuthRequest myRequest) {
        String errorMessage = myRequest.getKeys().get("message");
        Activity activity = (Activity) currentContext;
        activity.runOnUiThread(() -> {
            ServerFragment fragment = (ServerFragment)
                ((AppCompatActivity) activity).getSupportFragmentManager().findFragmentByTag("f3");
            if (fragment != null) {
                fragment.showError(errorMessage != null ? errorMessage : "Error !");
            }
        });
    }


    public void buildSession(AuthRequest myRequest) {
        if (myRequest == null || myRequest.getKeys() == null) {
            return;
        }

        SessionManager sessionManager = SessionManager.getInstance();

        // Récupère les données envoyées par le serveur
        String token = myRequest.getKeys().get("token");
        String username = myRequest.getKeys().get("username");
        String decksJson = myRequest.getKeys().get("decks");
        String unlockedsJson = myRequest.getKeys().get("unlocked");

        // Remplit la session
        sessionManager.setToken(token);
        sessionManager.setUsername(username);

        if (decksJson == null || decksJson.isEmpty()) {
            decksJson = "{}";
        }
        sessionManager.setDecksFromJson(decksJson);

        // Gestion des cartes débloquées
        if (unlockedsJson == null || unlockedsJson.isEmpty()) {
            unlockedsJson = "{}";
        }
        sessionManager.setUnlockedCardsFromJson(unlockedsJson);

        sessionManager.setActive(true);
    }

    public void launchSecondActivity(){
        if (currentContext instanceof Activity) {
            ((Activity) currentContext).runOnUiThread(() -> {
                Log.d("Auth","Login Sucess !!!!!!B");
                Intent intent = new Intent(currentContext, HomeActivity.class);
                currentContext.startActivity(intent);
            });
        }
    }

    public void launchSecondActivityAndPassBySplashScreen(){
        if (currentContext instanceof Activity) {
            ((Activity) currentContext).runOnUiThread(() -> {
                Intent intent = new Intent(currentContext, SplashActivity.class);
                intent.putExtra("redirect_home", true);
                currentContext.startActivity(intent);
            });
        }
    }

    /**
     * Closes the current user session.
     * <p>
     * This method clears all session data, marks the session as inactive,
     * and optionally closes the network connection.
     */
    public void closeSession() {
        SessionManager sessionManager = SessionManager.getInstance();

        // Clear all session data
        sessionManager.clearSession();

        // Optionally close Kryo connection
        if (kryoManager != null && kryoManager.getClient() != null && kryoManager.getClient().isConnected()) {
            kryoManager.closeClient();
        }

        Log.d("ClientManager", "Session closed and connection terminated.");
    }



    //---------
    // Setters & Getters
    //---------

    public KryoClientManager getKryoManager(){
        return kryoManager;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getIP() {
        return ip;
    }

    public void setIP(String ip) {
        this.ip = ip;
    }

    public Activity getCurrentContext() {
        return currentContext;
    }

    public void setCurrentContext(Activity currentContext) {
        this.currentContext = currentContext;
    }
}

