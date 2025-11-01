package io.github.android.callback.splash;

import android.app.Activity;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.ProgressBar;

import io.github.android.activity.LoginActivity;
import io.github.android.gui.animation.AnimatorBar;

public class SplashFailConnectionCallback implements Runnable {

    private final AnimatorBar bar;
    private final String message;
    private ProgressBar progressBar;
    private Activity activity;

    public SplashFailConnectionCallback(AnimatorBar bar, ProgressBar progressBar, String message, Activity activity) {
        this.bar = bar;
        this.progressBar = progressBar;
        this.message = message;
        this.activity = activity;
    }

    @Override
    public void run() {
        if (bar != null) {
            new Handler(Looper.getMainLooper()).post(() -> {
                bar.addStep(new AnimatorBar.Step(
                    progressBar,
                    "progress",
                    new float[]{50f, 75f}, // remplir jusqu’au bout
                    800,
                    message,                // afficher le message d’erreur
                    () -> Log.e("Splash", "Connexion échouée : " + message),
                    () -> {
                        // Quand l’animation est finie → retour au launcher
                        // Ici tu peux déclencher une redirection si tu passes l’Activity au constructeur
                    },
                    null
                )).run();

                bar.addStep(new AnimatorBar.Step(
                    progressBar,
                    "progress",
                    new float[]{75f, 100f}, // remplir jusqu’au bout
                    800,
                    "Redirecting to main launcher",                // afficher le message d’erreur
                    () -> Log.e("Splash", "Connexion échouée : " + message),
                    () -> {
                        // Quand la deuxième animation est terminée → redirection
                        Intent intent = new Intent(activity, LoginActivity.class);
                        intent.putExtra("login_error", message);
                        activity.startActivity(intent);
                        activity.finish();
                    },
                    null
                )).run();



            });
        } else {
            Log.e("Splash Error", "AnimatorBar est null, message=" + message);
        }
    }

}
