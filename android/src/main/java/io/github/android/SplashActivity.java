package io.github.android;

import static io.github.android.config.ClientDefaultConfig.CLIENT_SECURE;
import static io.github.android.config.ClientDefaultConfig.SERVER_PREFS;
import static io.github.android.utils.OtherUtils.checkAutoConnect;
import static io.github.android.utils.OtherUtils.initClientConfig;

import android.os.Bundle;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.HashMap;

import io.github.android.callback.splash.LaunchLauncherActivityCallBack;
import io.github.android.callback.splash.LaunchSecondActivityCallBack;
import io.github.android.callback.splash.SplashFailConnectionCallback;
import io.github.android.callback.splash.SplashLoginAuthCallback;
import io.github.android.callback.splash.SplashServerResponseCheck;
import io.github.android.gui.AnimatorBar;
import io.github.android.listeners.AuthClientListener;
import io.github.android.manager.ClientManager;
import io.github.android.utils.PrefsUtils;
import io.github.core.client_engine.manager.KryoClientManager;
import io.github.fortheoil.R;

public class SplashActivity extends AppCompatActivity {

    private ClientManager clientManager;
    private ProgressBar splashProgress;

    private AuthClientListener myListener;
    private AnimatorBar bar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_screen);

        clientManager = ClientManager.getInstance();
        this.clientManager.setCurrentContext(this);
        initClientConfig(this);

        TextView splashText = findViewById(R.id.splashText);
        splashProgress = findViewById(R.id.splashProgress);

        this.bar = new AnimatorBar(splashProgress, splashText);


        redirect();
    }

    public void redirect(){

        bar.addStep(new AnimatorBar.Step(
            splashProgress,
            "progress",
            new float[]{0f, 25f},
            800,
            "Initialisation...",
            null,
            null,
            null
        )).run();


        boolean forceLogin = getIntent().getBooleanExtra("forceLogin", false);
        boolean confirmLogin = getIntent().getBooleanExtra("redirect_home", false);
        boolean lost_connection_Login = getIntent().getBooleanExtra("lost_connection", false);



        if (lost_connection_Login){
            bar.addStep(new AnimatorBar.Step(
                splashProgress,
                "progress",
                new float[]{25f, 50f},
                800,
                "Connection lost !",
                null,
                null,
                null
            )).run();

            bar.addStep(new AnimatorBar.Step(
                splashProgress,
                "progress",
                new float[]{50f, 100f},
                800,
                "Loading launcher !",
                null,
                new LaunchLauncherActivityCallBack(this, "Connection lost..."),
                null
            )).run();
            return;
        }


        if (confirmLogin){

            bar.addStep(new AnimatorBar.Step(
                splashProgress,
                "progress",
                new float[]{25f, 100f},
                800,
                "Loading the home...",
                null,
                new LaunchSecondActivityCallBack(this),
                null
            )).run();
            return;
        }



        // IF we are ordered to go back to the login activity
        if (forceLogin) {
            bar.addStep(new AnimatorBar.Step(
                splashProgress,
                "progress",
                new float[]{25f, 100f},
                800,
                "Going back to the launcher...",
                null,
                new LaunchLauncherActivityCallBack(this),
                null
            )).run();

        }

        // If client want auto connect
        else if (checkAutoConnect(this)){

            bar.addStep(new AnimatorBar.Step(
                splashProgress,
                "progress",
                new float[]{25f, 50f},
                800,
                "Connecting to the server...",
                null,
                null,
                null
            )).run();

            autoLogin();

        }

        //Default case
        else {
            bar.addStep(new AnimatorBar.Step(
                splashProgress,
                "progress",
                new float[]{25f, 100f},
                800,
                "Loading the launcher...",
                null,
                new LaunchLauncherActivityCallBack(this),
                null
            )).run();
        }

    }


    public void autoLogin() {
        HashMap<String, String> myMap = PrefsUtils.loadPrefs(SERVER_PREFS, this);

        if ("true".equals(myMap.getOrDefault("auto_login", "false"))) {
            Log.d("AutoLogin","Trying to auto login");

            HashMap<String, String> mySecureMap = PrefsUtils.loadEncryptedPrefs(CLIENT_SECURE, this);

            String email = mySecureMap.getOrDefault("email","");
            String password = mySecureMap.getOrDefault("password","");

            if (!email.isEmpty() && !password.isEmpty()) {
                Log.d("AutoLogin","Sending the auto login");

                KryoClientManager kryoManager = clientManager.getKryoManager();

                myListener = AuthClientListener.getInstance(this, new SplashServerResponseCheck(this, bar, splashProgress) );
                kryoManager.addListener(myListener);

                kryoManager.start();

                kryoManager.connect(
                    clientManager.getIP(),
                    clientManager.getPort(),
                    new SplashLoginAuthCallback(email, password, bar, splashProgress, this),
                    new SplashFailConnectionCallback(bar, splashProgress,"Error server unreachable...", this)
                );
                return;
            }

        }

        bar.addStep(new AnimatorBar.Step(
            splashProgress,
            "progress",
            new float[]{50f, 75f},
            800,
            "Invalid credentials !",
            null,
            new LaunchLauncherActivityCallBack(this),
            null
        )).run();

    }












}

