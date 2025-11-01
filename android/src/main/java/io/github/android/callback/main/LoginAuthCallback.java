package io.github.android.callback.main;

import android.util.Log;

import io.github.android.manager.ClientManager;

public class LoginAuthCallback implements Runnable {

    private final String email;
    private final String password;

    public LoginAuthCallback(String email, String password) {
        this.email = email;
        this.password = password;
    }

    @Override
    public void run(){
        Log.d("For The Oil", "Login AuthCallBack ...");
        ClientManager.getInstance().login(email, password);
    }
}
