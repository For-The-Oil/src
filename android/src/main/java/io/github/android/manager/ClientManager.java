package io.github.android.manager;

import static io.github.android.utils.UiUtils.showMessage;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import io.github.android.activity.SecondActivity;
import io.github.android.gui.fragment.launcher.LoginFragment;
import io.github.android.gui.fragment.launcher.RegisterFragment;
import io.github.android.gui.fragment.launcher.ServerFragment;
import io.github.core.client_engine.factory.KryoMessagePackager;
import io.github.core.client_engine.factory.RequestFactory;
import io.github.core.client_engine.manager.KryoClientManager;
import io.github.shared.local.data.network.KryoMessage;
import io.github.shared.local.data.requests.AuthRequest;

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
        buildSession(myRequest);
        launchSecondActivity();
    }

    public void registerSuccess(AuthRequest myRequest){
        buildSession(myRequest);
        launchSecondActivity();
    }

    public void tokenSuccess(AuthRequest myRequest){
        buildSession(myRequest);
        launchSecondActivity();
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

        // Remplit la session
        sessionManager.setToken(token);
        sessionManager.setUsername(username);
        if (decksJson == null ||decksJson.isEmpty()){
            decksJson="{}";
        }
        sessionManager.setDecksFromJson(decksJson);
        sessionManager.setActive(true);
    }

    public void launchSecondActivity(){
        if (currentContext instanceof Activity) {
            ((Activity) currentContext).runOnUiThread(() -> {
                Intent intent = new Intent(currentContext, SecondActivity.class);
                currentContext.startActivity(intent);
            });
        }
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

