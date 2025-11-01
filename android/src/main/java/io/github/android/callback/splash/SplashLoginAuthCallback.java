package io.github.android.callback.splash;


import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.View;

import io.github.android.activity.SecondActivity;
import io.github.android.gui.AnimatorBar;
import io.github.android.manager.ClientManager;

public class SplashLoginAuthCallback implements Runnable {

    private final String email;
    private final String password;
    private final AnimatorBar bar;
    private final View splashProgress;
    private final Activity activity; // pour exécuter sur le thread UI

    public SplashLoginAuthCallback(String email, String password, AnimatorBar bar, View splashProgress, Activity activity) {
        this.email = email;
        this.password = password;
        this.bar = bar;
        this.splashProgress = splashProgress;
        this.activity = activity;
    }

    @Override
    public void run() {
        Log.d("AutoLogin", "SplashLoginAuthCallback started...");

        // Lancer la requête de login
        ClientManager.getInstance().login(email, password);

        activity.runOnUiThread(() -> {
            bar.addStep(new AnimatorBar.Step(
                splashProgress,
                "progress",
                new float[]{50f, 75f}, // progression visuelle
                800,                    // durée
                "Connected !",          // message affiché
                null,
                null,
                null
            )).run();

            Log.d("AutoLogin", "Login successful animation triggered");

        });
    }
}
