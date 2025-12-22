package io.github.android.activity;

import static io.github.android.config.ClientDefaultConfig.CLIENT_SECURE;
import static io.github.android.config.ClientDefaultConfig.INIT_WAITING_TIME;
import static io.github.android.config.ClientDefaultConfig.SERVER_PREFS;
import static io.github.android.utils.OtherUtils.initClientConfig;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;

import java.util.HashMap;

import io.github.android.config.UiConfig;
import io.github.android.gui.fragment.launcher.LoadingFragment;
import io.github.android.listeners.ClientListener;
import io.github.android.manager.ClientManager;
import io.github.android.utils.PrefsUtils;
import io.github.android.utils.RedirectUtils;
import io.github.core.game_engine.factory.ModelFactory;
import io.github.core.game_engine.factory.SceneFactory;
import io.github.fortheoil.R;
import io.github.shared.data.enums_types.CellType;
import io.github.shared.data.enums_types.EntityType;
import io.github.shared.data.requests.AuthRequest;

/**
 * <h1>Splash Activity</h1>
 *
 * <p>Main activity that is launched at the start of the application.
 * This activity try to auto login the client on the last registered server if the client has activated the auto login.
 * If it fails, it redirect the client to the login screen.</p>
 *
 *
 */
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
        initPath();
    }

    public void handleClassicConnectionStep() {
        loadingFragment.show();
        phaseInit();
    }

    /**
     * 1) Start the launching animation and then start the next function.
     */
    private void phaseInit() {
        loadingFragment.animateProgress(0f, 12f, INIT_WAITING_TIME, "Initialisation", null, this::phaseCheckAutoLogin);
    }

    /**
     * 2) check if the user has activated the auto login
     *  a) if false, redirecting to the Login Activity
     *  b) else, redirecting to the next step
     */
    private void phaseCheckAutoLogin() {
        if (!isAutoLoginOn(getApplicationContext())) {
            loadingFragment.animateProgress(12f, 100f, INIT_WAITING_TIME, "Loading connection page", null, () -> {
                RedirectUtils.simpleRedirectAndClearStack(this, LoginActivity.class);
            });
        } else {
            phaseCheckSavedCredentials();
        }
    }

    /**
     * 3) check if the saved credentials are valids
     *  a) if invalid, redirecting
     *  b) else redirecting to the next step
     */
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


    /**
     * 4) Try to to connect to the saved server (IP, PORT)
     *  a) failure : redirecting to login activity
     *  b) else, redirecting to the next step
     */
    private void phaseConnectToServer() {
        loadingFragment.animateProgress(25f, 50f, INIT_WAITING_TIME, "Connecting to the server...", null, () -> {
            try {
                ClientManager.getInstance().getKryoManager().start();
            }
            catch (Exception e){
                Log.d("For The Oil", "Tried to start a connection while already connected...");
            }

            ClientManager.getInstance().getKryoManager().connect(
                clientManager.getIP(),
                clientManager.getPort(),
                () -> runOnUiThread(this::phaseConnectionSuccess), // Function called in case of success
                () -> runOnUiThread(this::phaseConnectionFailure) // Function called in case of failure
            );
        });
    }


    /**
     * 5) a) inform the client that we successfully connected to the server
     */
    private void phaseConnectionSuccess() {
        loadingFragment.animateProgress(50f, 75f, INIT_WAITING_TIME, "Connected !", null, this::phaseSendCredentials);
    }


    /**
     * 5) b) inform the client that we couldn't connect to the server
     */
    private void phaseConnectionFailure() {
        ClientManager.getInstance().closeSession();
        loadingFragment.setGradient(UiConfig.MEDIUM_RED, UiConfig.DARK_RED);
        loadingFragment.animateProgress(50f, 100f, INIT_WAITING_TIME, "Server not found !", null,
            () -> RedirectUtils.simpleRedirectAndClearStack(this, LoginActivity.class, "login_error", "Server not found")
        );
    }

    /**
     * 6) Sending the credentials to the server
     * The next steps are describe in the listener below.
     *
     */
    private void phaseSendCredentials() {

        // TODO : Manage the case where the server never send back an answer !

        loadingFragment.animateProgress(75f, 85f, INIT_WAITING_TIME, "Sending credentials !", null, () -> {
            new Thread(() -> {ClientManager.getInstance().login(email, password);}).start();
        });
    }




    // -----------
    // Intern Logic
    // -----------

    /**
     * This function check if the user has activated the auto login
     * @param context
     * @return
     */
    public boolean isAutoLoginOn(Context context) {
        HashMap<String, String> prefs = PrefsUtils.loadPrefs(SERVER_PREFS, context);
        return "true".equalsIgnoreCase(prefs.getOrDefault("auto_login", "false"));
    }

    /**
     * This function check if the user has any not null saved credentials.
     * @param context
     * @return
     */
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

    /**
     * Function that init the loading fragment with the animation.
     */
    private void setupLoadingFragment(){
        loadingFragment = new LoadingFragment();
        getSupportFragmentManager().beginTransaction()
            .add(R.id.loadingOverlay, loadingFragment, "LOADING_FRAGMENT")
            .commit();
        FrameLayout overlay = findViewById(R.id.loadingOverlay);
        overlay.setOnTouchListener((v, event) -> overlay.getVisibility() == View.VISIBLE);
        overlay.post(this::handleClassicConnectionStep);
    }



    private void initPath(){

        HashMap<Object, String> texture_map = new HashMap<>();
        HashMap<Object, String> models_map = new HashMap<>();

        //ground
        texture_map.put(CellType.ROAD, "textures/concrete.png");
        texture_map.put(CellType.GRASS, "textures/grass.png");
        texture_map.put(CellType.WATER, "textures/water.png");

        //units
        models_map.put(EntityType.TANK, "models/default_tank.glb");
        models_map.put(EntityType.BIKE, "models/default_bike.glb");
        models_map.put(EntityType.JEEP, "models/default_jeep.glb");
        models_map.put(EntityType.BASE, "models/base.glb");
        models_map.put(EntityType.FACTORY, "models/factory.glb");
        models_map.put(EntityType.GARAGE, "models/garage.glb");
        models_map.put(EntityType.test, "models/test.glb");
        models_map.put(EntityType.BARRACK, "models/barrack.glb");
        models_map.put(EntityType.MINE, "models/mine.glb");
        models_map.put(EntityType.DERRICK, "models/derrick.glb");

        ModelFactory.initINSTANCE(texture_map);
        SceneFactory.initINSTANCE(models_map);

    }

}

