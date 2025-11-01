package io.github.android.callback.splash;

import android.app.Activity;
import android.content.Intent;

import io.github.android.activity.LoginActivity;

public class LaunchLauncherActivityCallBack implements Runnable{


    private Activity activity;
    private String error;

    public LaunchLauncherActivityCallBack(Activity activity){
        this.activity = activity;
        this.error = null;
    }

    public LaunchLauncherActivityCallBack(Activity activity, String error){
        this.activity = activity;
        this.error = error;
    }


    @Override
    public void run() {
        Intent intent = new Intent(activity, LoginActivity.class);

        if (error != null && !error.isEmpty()) {
            intent.putExtra("login_error", error);
        }

        activity.startActivity(intent);
        activity.finish();
    }



}
