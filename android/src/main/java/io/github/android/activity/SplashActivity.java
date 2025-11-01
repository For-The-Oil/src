package io.github.android.activity;

import static io.github.android.config.ClientDefaultConfig.CLIENT_SECURE;
import static io.github.android.config.ClientDefaultConfig.INIT_WAITING_TIME;
import static io.github.android.config.ClientDefaultConfig.SERVER_PREFS;
import static io.github.android.utils.OtherUtils.initClientConfig;

import android.content.Context;
import android.os.Bundle;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.Nullable;

import java.util.HashMap;

import io.github.android.gui.animation.AnimatorBar;
import io.github.android.listeners.ClientListener;
import io.github.android.manager.ClientManager;
import io.github.android.utils.PrefsUtils;
import io.github.android.utils.RedirectUtils;
import io.github.fortheoil.R;
import io.github.shared.local.data.requests.AuthRequest;

public class SplashActivity extends BaseActivity {

    private ClientManager clientManager;
    private ProgressBar splashProgress;
    private AnimatorBar bar;

    private String email;
    private String password;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_screen);

        clientManager = ClientManager.getInstance();
        this.clientManager.setCurrentContext(this);

        initClientConfig(this);
        initListener();
        initUI();

        handleClassicConnectionStep();
    }

    public void handleClassicConnectionStep() {
        phaseInit();
    }

    private void phaseInit() {
        animateProgress(0f, 12f, INIT_WAITING_TIME, "Initialisation", null, this::phaseCheckAutoLogin);
    }

    private void phaseCheckAutoLogin() {
        if (!isAutoLoginOn(getApplicationContext())) {
            animateProgress(12f, 100f, INIT_WAITING_TIME, "Loading connection page", null, () -> {
                RedirectUtils.withRedirectToLauncher(this, null).safeKill();
            });
        } else {
            phaseCheckSavedCredentials();
        }
    }

    private void phaseCheckSavedCredentials() {
        animateProgress(12f, 25f, INIT_WAITING_TIME, "Checking saved credentials...", null, () -> {
            if (!checkSavedCredentials(getApplicationContext())) {
                animateProgress(25f, 100f, INIT_WAITING_TIME, "Loading connection page", null,
                    () -> {
                        RedirectUtils.withRedirectToLauncher(this, "Invalid credentials").safeKill();}
                );
            } else {
                phaseConnectToServer();
            }
        });
    }

    private void phaseConnectToServer() {
        animateProgress(25f, 50f, INIT_WAITING_TIME, "Connecting to the server...", null, () -> {
            ClientManager.getInstance().getKryoManager().start();
            ClientManager.getInstance().getKryoManager().connect(
                clientManager.getIP(),
                clientManager.getPort(),
                () -> runOnUiThread(this::phaseConnectionSuccess),
                () -> runOnUiThread(this::phaseConnectionFailure)
            );
        });
    }

    private void phaseConnectionSuccess() {
        animateProgress(50f, 75f, INIT_WAITING_TIME, "Connected !", null, this::phaseSendCredentials);
    }

    private void phaseConnectionFailure() {
        ClientManager.getInstance().closeSession();
        animateProgress(50f, 100f, INIT_WAITING_TIME, "Server not found !", null,
            () -> RedirectUtils.withRedirectToLauncher(this, "Server not found...").safeKill()
        );
    }

    private void phaseSendCredentials() {
        animateProgress(75f, 85f, INIT_WAITING_TIME, "Sending credentials !", null, () -> {
            new Thread(() -> {ClientManager.getInstance().login(email, password);}).start();
        });
    }










    // -----------
    // Intern Logic
    // -----------

    public boolean isAutoLoginOn(Context context) {
        HashMap<String, String> prefs = PrefsUtils.loadPrefs(SERVER_PREFS, context);
        return "true".equalsIgnoreCase(prefs.getOrDefault("auto_login", "false"));
    }

    public boolean checkSavedCredentials(Context context) {
        HashMap<String, String> creds = PrefsUtils.loadEncryptedPrefs(CLIENT_SECURE, context);
        email = creds.getOrDefault("email", "");
        password = creds.getOrDefault("password", "");
        return !email.isEmpty() && !password.isEmpty();
    }


    /**
     * This function set the content of the ClientListener.
     * We tell him what to do if we encounter a AuthRequest object.
     */
    public void initListener(){

        // Init of the listenr / destroy artifacts from a possible previous use
        ClientListener myListener = ClientListener.getInstance();
        myListener.clearCallbacks();
        myListener.setCurrentActivity(this); //Important, we init the current activity

        // The content of the listener for the AuthRequest object
        // Thoses behaviour are defined here because only used here
        myListener.onMessage(AuthRequest.class, authRequest  -> {
            String message = authRequest.getKeys().getOrDefault("message","Error");

            switch (authRequest.getMode()){
                case LOGIN_FAIL:
                    ClientManager.getInstance().closeSession();
                    animateProgress(85f, 100f, INIT_WAITING_TIME, "Bad credentials !", null,
                        () -> RedirectUtils.withRedirectToLauncher(this, message).safeKill()
                    );
                    break;
                case LOGIN_SUCCESS:
                    ClientManager.getInstance().buildSession(authRequest);
                    animateProgress(85f, 100f, INIT_WAITING_TIME, "Success !", null,
                        () -> RedirectUtils.withRedirectToMainMenu(this).safeKill()
                    );
                    break;
                default:
                    animateProgress(85f, 100f, INIT_WAITING_TIME, "Unexpected responses !", null,
                        () -> RedirectUtils.withRedirectToLauncher(this, message).safeKill()
                    );
            }
        }, true);
    }




    // -----------
    // UI Intern Logic
    // -----------

    /**
     * Init the UI interface and save in the application all the usefull data.
     */
    private void initUI() {
        TextView splashText = findViewById(R.id.splashText);
        splashProgress = findViewById(R.id.splashProgress);
        this.bar = new AnimatorBar(splashProgress, splashText);
    }

    /**
     * Function that animate the progress of an animation bar.
     * @param start
     * @param end
     * @param duration
     * @param message
     * @param onStart
     * @param onEnd
     */
    public void animateProgress(float start, float end, long duration, String message, @Nullable Runnable onStart, @Nullable Runnable onEnd) {
        bar.addStep(new AnimatorBar.Step(
            splashProgress,
            "progress",
            new float[]{start, end},
            duration,
            message,
            onStart,
            onEnd,
            null
        )).run();
    }



}

