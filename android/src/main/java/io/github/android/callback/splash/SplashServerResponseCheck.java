package io.github.android.callback.splash;

import android.app.Activity;
import android.util.Log;
import android.widget.ProgressBar;

import io.github.android.callback.AuthCallBack;
import io.github.android.gui.animation.AnimatorBar;
import io.github.shared.data.requests.AuthRequest;

public class SplashServerResponseCheck implements AuthCallBack {


    private final Activity activity;
    private final AnimatorBar bar;
    private final ProgressBar progressBar;

    public SplashServerResponseCheck(Activity activity, AnimatorBar bar, ProgressBar progressBar){
        this.activity = activity;
        this.bar = bar;
        this.progressBar = progressBar;
    }
    public void onLoginSuccess(AuthRequest req) {
        Log.d("Info", "Login succesfull !");
        bar.addStep(new AnimatorBar.Step(
            progressBar,
            "progress",
            new float[]{75f, 100f}, // progression visuelle
            800,                    // durée
            "Redirecting to home...",          // message affiché
            null,
            new LaunchSecondActivityCallBack(activity),
            null
        )).run();
    }
    public void onLoginFailure(AuthRequest req) {
        Log.d("Info", "Login error ! !");
        bar.addStep(new AnimatorBar.Step(
            progressBar,
            "progress",
            new float[]{75f, 100f}, // progression visuelle
            800,                    // durée
            "Bad credentials, redirecting...",          // message affiché
            null,
            new LaunchLauncherActivityCallBack(activity, req.getKeys().getOrDefault("message","Error")),
            null
        )).run();
    }

}
