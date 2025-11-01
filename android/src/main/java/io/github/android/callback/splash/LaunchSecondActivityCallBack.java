package io.github.android.callback.splash;

import android.app.Activity;
import android.content.Intent;

import io.github.android.activity.SecondActivity;

public class LaunchSecondActivityCallBack implements Runnable{

    private Activity activity;
    public LaunchSecondActivityCallBack(Activity activity){
        this.activity = activity;
    }

    @Override
    public void run(){
        Intent intent = new Intent(activity, SecondActivity.class);
        activity.startActivity(intent);
        activity.finish();
    }

}
