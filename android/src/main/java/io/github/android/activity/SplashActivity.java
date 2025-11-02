package io.github.android.activity;

import static io.github.android.config.ClientDefaultConfig.CLIENT_SECURE;
import static io.github.android.config.ClientDefaultConfig.INIT_WAITING_TIME;
import static io.github.android.config.ClientDefaultConfig.SERVER_PREFS;
import static io.github.android.utils.OtherUtils.initClientConfig;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.Nullable;

import java.util.HashMap;

import io.github.android.config.UiConfig;
import io.github.android.gui.animation.AnimatorBar;
import io.github.android.gui.fragment.launcher.LoadingFragment;
import io.github.android.listeners.ClientListener;
import io.github.android.manager.ClientManager;
import io.github.android.utils.PrefsUtils;
import io.github.android.utils.RedirectUtils;
import io.github.fortheoil.R;
import io.github.shared.local.data.requests.AuthRequest;

public class SplashActivity extends BaseActivity {

    private ClientManager clientManager;
    private LoadingFragment loadingFragment;

    private String email;
    private String password;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_activity);

        clientManager = ClientManager.getInstance();
        this.clientManager.setCurrentContext(this);

        initClientConfig(this);
        initListener();
        setupLoadingFragment();
    }

    public void handleClassicConnectionStep() {
        loadingFragment.show();
        phaseInit();
    }

    private void phaseInit() {
        loadingFragment.animateProgress(0f, 12f, INIT_WAITING_TIME, "Initialisation", null, this::phaseCheckAutoLogin);
    }

    private void phaseCheckAutoLogin() {
        if (!isAutoLoginOn(getApplicationContext())) {
            loadingFragment.animateProgress(12f, 100f, INIT_WAITING_TIME, "Loading connection page", null, () -> {
                RedirectUtils.simpleRedirectAndClearStack(this, LoginActivity.class);
            });
        } else {
            phaseCheckSavedCredentials();
        }
    }

    private void phaseCheckSavedCredentials() {
        loadingFragment.animateProgress(12f, 25f, INIT_WAITING_TIME, "Checking saved credentials...", null, () -> {
            if (!checkSavedCredentials(getApplicationContext())) {
                loadingFragment.animateProgress(25f, 100f, INIT_WAITING_TIME, "Loading connection page", null,
                    () -> {
                        RedirectUtils.simpleRedirectAndClearStack(this, LoginActivity.class, "login_error", "Invalid Credentials");
                }
                );
            } else {
                phaseConnectToServer();
            }
        });
    }

    private void phaseConnectToServer() {
        loadingFragment.animateProgress(25f, 50f, INIT_WAITING_TIME, "Connecting to the server...", null, () -> {
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
        loadingFragment.animateProgress(50f, 75f, INIT_WAITING_TIME, "Connected !", null, this::phaseSendCredentials);
    }

    private void phaseConnectionFailure() {
        ClientManager.getInstance().closeSession();
        loadingFragment.setGradient(UiConfig.MEDIUM_RED, UiConfig.DARK_RED);
        loadingFragment.animateProgress(50f, 100f, INIT_WAITING_TIME, "Server not found !", null,
            () -> RedirectUtils.simpleRedirectAndClearStack(this, LoginActivity.class, "login_error", "Server not found")
        );
    }

    private void phaseSendCredentials() {
        loadingFragment.animateProgress(75f, 85f, INIT_WAITING_TIME, "Sending credentials !", null, () -> {
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
                    loadingFragment.setGradient(UiConfig.MEDIUM_RED, UiConfig.DARK_RED);
                    ClientManager.getInstance().closeSession();
                    loadingFragment.animateProgress(85f, 100f, INIT_WAITING_TIME, "Bad credentials !", null,
                        () -> RedirectUtils.simpleRedirectAndClearStack(this, LoginActivity.class, "login_error", message)
                    );
                    break;
                case LOGIN_SUCCESS:
                    ClientManager.getInstance().buildSession(authRequest);
                    loadingFragment.animateProgress(85f, 100f, INIT_WAITING_TIME, "Success !", null,
                        () -> RedirectUtils.simpleRedirectAndClearStack(this, HomeActivity.class)
                    );
                    break;
                default:
                    loadingFragment.setGradient(UiConfig.MEDIUM_RED, UiConfig.DARK_RED);
                    loadingFragment.animateProgress(85f, 100f, INIT_WAITING_TIME, "Unexpected responses !", null,
                        () -> RedirectUtils.simpleRedirectAndClearStack(this, LoginActivity.class, "login_error", message)
                    );
            }
        }, true);
    }




    // -----------
    // UI Intern Logic
    // -----------

    private void setupLoadingFragment(){
        loadingFragment = new LoadingFragment();
        getSupportFragmentManager().beginTransaction()
            .add(R.id.loadingOverlay, loadingFragment, "LOADING_FRAGMENT")
            .commit();
        FrameLayout overlay = findViewById(R.id.loadingOverlay);
        overlay.setOnTouchListener((v, event) -> overlay.getVisibility() == View.VISIBLE);
        overlay.post(this::handleClassicConnectionStep);
    }

}

