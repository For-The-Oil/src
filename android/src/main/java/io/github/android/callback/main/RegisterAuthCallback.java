package io.github.android.callback.main;

import android.util.Log;

import io.github.android.manager.ClientManager;

public class RegisterAuthCallback implements Runnable{

    private final String email;
    private final String username;
    private final String password;
    private final String password2;

    public RegisterAuthCallback(String email, String username, String password, String password2) {
        this.email = email;
        this.username = username;
        this.password = password;
        this.password2 = password2;
    }

    @Override
    public void run(){
        Log.d("For The Oil", "Register AuthCallBack ...");
        ClientManager.getInstance().register(email, username, password, password2);
    }

}
