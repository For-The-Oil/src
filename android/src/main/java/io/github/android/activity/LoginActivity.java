package io.github.android.activity;

import static io.github.android.config.ClientDefaultConfig.CLIENT_SECURE;
import static io.github.android.config.ClientDefaultConfig.INIT_WAITING_TIME;
import static io.github.android.config.ClientDefaultConfig.SERVER_PREFS;
import static io.github.android.utils.FormatUtils.isValidHostname;
import static io.github.android.utils.FormatUtils.isValidIPAddress;

import android.os.Bundle;

import io.github.android.config.ServerDefaultConfig;
import io.github.android.config.UiConfig;
import io.github.android.gui.adapter.LauncherAdapter;
import io.github.android.gui.fragment.launcher.LoadingFragment;
import io.github.android.gui.fragment.launcher.LoginFragment;
import io.github.android.gui.fragment.launcher.RegisterFragment;
import io.github.android.gui.fragment.launcher.ServerFragment;
import io.github.android.listeners.ClientListener;
import io.github.android.manager.ClientManager;
import io.github.android.utils.PrefsUtils;
import io.github.android.utils.RedirectUtils;
import io.github.android.utils.UiUtils;
import io.github.core.client_engine.manager.KryoClientManager;
import io.github.core.client_engine.utils.FormChecker;
import io.github.fortheoil.R;
import io.github.shared.data.requests.AuthRequest;

import androidx.viewpager2.widget.ViewPager2;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.util.Log;

import java.util.HashMap;


/**
 * <h1>Login Activity</h1>
 *
 * <p>This activity is composed of 3 fragments : </p>
 * <ul>
 *     <li>Server Fragment : Where we can change what server / port we want to access.</li>
 *     <li>Register Fragment : Where the client can try to register to </li>
 *     <li>Login Fragment : A fragment where the user can input his credentials in order to connect. He can also activate the auto-connection.</li>
 * </ul>
 */
public class LoginActivity extends BaseActivity {
    private LinearLayout dotsLayout;
    private ClientManager clientManager;
    private LauncherAdapter adapter;

    private LoadingFragment loadingFragment;
    private String email;
    private String password;
    private String confirmPassword;
    private String username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);

        this.clientManager = ClientManager.getInstance();
        this.clientManager.setCurrentContext(this);

        // Vérifier si on a reçu un message d’erreur
        String loginError = getIntent().getStringExtra("login_error");
        Log.d("For the oil", "Error to load ! " + loginError);

        setupViewPager(loginError);
        setupLoadingFragment();

        initListener();

        Log.d("For the oil", "Are we connected ? " + ClientManager.getInstance().getKryoManager().getClient().isConnected());

        //initClientConfig(this);
        //hideErrors();

    }


    @Override
    protected void onResume(){
        super.onResume();
        hideErrors();
    }


    // -------------------------
    // UI Interaction Methods
    // -------------------------
    // The following methods are triggered by user actions in the layout.
    // They handle login and registration input validation, UI feedback,
    // and communication with the ClientManager to perform authentication.

    /**
     * Handles the login button click event.
     *
     * Retrieves the email and password input fields from the layout,
     * validates the email format, checks server connection, and then
     * delegates the login request to the ClientManager.
     *
     * @param view The login button that was clicked.
     */
    public void login(View view) {
        EditText emailField = findViewById(R.id.loginEmail);
        EditText passwordField = findViewById(R.id.loginPassword);
        CheckBox checkField = findViewById(R.id.autoLogin);
        TextView errorField = findViewById(R.id.loginMessage);

        UiUtils.hideMessage(errorField);

        email = emailField.getText().toString().trim();
        password = passwordField.getText().toString().trim();

        HashMap<String, String> myPrefs = new HashMap<>();

        if (checkField.isChecked()){
            /// Si le user active l'auto Login
            HashMap<String, String> myMap = new HashMap<>();
            Log.d("AutoLogin","Saving the auto login settings");
            myMap.put("email",email);
            myMap.put("password",password);
            myPrefs.put("auto_login","true");
            PrefsUtils.saveEncryptedPrefs(CLIENT_SECURE, myMap, this);
        } else {
            /// Si le user ne l'active pas
            Log.d("AutoLogin","Saving the default auto login settings");
            myPrefs.put("auto_login","false");
        }

        PrefsUtils.savePrefs(SERVER_PREFS, myPrefs, this);


        if (!FormChecker.isValidEmail(email)) {
            UiUtils.showMessage(findViewById(R.id.loginMessage), "Email invalide !");
            return;
        }

        // Send to the server the Input
        Log.d("LoginActivity", "Login requested: " + email);

        ClientManager clientManager = ClientManager.getInstance();
        KryoClientManager kryoManager = clientManager.getKryoManager();

        kryoManager.start();
        loadingFragment.setDefaultGradient();
        loadingFragment.show();
        phaseConnectToServer();

    }


    /**
     * Handles the register button click event.
     *
     * Retrieves the username, email, password, and confirm password fields from the layout,
     * validates the inputs (email format, username, password match, password strength),
     * checks server connection, and then delegates the registration request to the ClientManager.
     *
     * @param view The register button that was clicked.
     */
    public void register(View view) {

        // First we collect all the field that were filled by the user
        EditText usernameField = findViewById(R.id.signupUsername);
        EditText emailField = findViewById(R.id.signupEmail);
        EditText passwordField = findViewById(R.id.signupPassword);
        EditText confirmPasswordField = findViewById(R.id.signupConfirmPassword);
        TextView errorField = findViewById(R.id.registerMessage);

        UiUtils.hideMessage(errorField);


        // We stringify theses fields
        username = usernameField.getText().toString().trim();
        email = emailField.getText().toString().trim();
        password = passwordField.getText().toString().trim();
        confirmPassword = confirmPasswordField.getText().toString().trim();

        // Validate the inputs
        String errorMessage = validateRegistrationInput(username, email, password, confirmPassword, clientManager);
        if (errorMessage != null) {
            UiUtils.showMessage(errorField, errorMessage);
            return;
        }

        // Send to the server the Input
        Log.d("LoginActivity", "Register requested: " + email);


        ClientManager clientManager = ClientManager.getInstance();
        KryoClientManager kryoManager = clientManager.getKryoManager();

        kryoManager.start();

        loadingFragment.setDefaultGradient();
        loadingFragment.show();
        phaseConnectToServerForRegister();

    }


    /**
     * <h3>Saves the server configuration</h3>
     * <p>
     * This method retrieves the IP/hostname and port from the user input fields,
     * validates them, and saves them in the <code>ClientManager</code>. Error or
     * success messages are displayed in the <code>serverMessage</code> area.
     * </p>
     *
     * @param view the button clicked to trigger the save
     */

    public void registerServer(View view) {
        ClientManager myClientManager = ClientManager.getInstance();

        EditText ipField = findViewById(R.id.serverIpField);
        EditText portField = findViewById(R.id.serverPortField);

        String ip = ipField.getText().toString().trim();
        String portText = portField.getText().toString().trim();

        if (ip.isEmpty()) {
            UiUtils.showMessage(findViewById(R.id.serverMessage), "Adresse IP/hostname invalide !");
            return;
        }

        if (!isValidIPAddress(ip) && !isValidHostname(ip)) {
            UiUtils.showMessage(findViewById(R.id.serverMessage), "Adresse IP/hostname incorrecte !");
            return;
        }

        try {
            int port = Integer.parseInt(portText);
            if (port < 1 || port > 65535) {
                UiUtils.showMessage(findViewById(R.id.serverMessage), "Port doit être entre 1 et 65535 !");
                return;
            }

            // Mets à jour ton client
            myClientManager.setPort(port);
            myClientManager.setIP(ip);

            // Sauvegarde dans les prefs
            HashMap<String, String> prefs = new HashMap<>();
            prefs.put("server_ip", ip);
            prefs.put("server_port", String.valueOf(port));
            PrefsUtils.savePrefs(SERVER_PREFS, prefs, this);

            UiUtils.showMessage(findViewById(R.id.serverMessage), "Paramètres enregistrés !");

        } catch (NumberFormatException e) {
            UiUtils.showMessage(findViewById(R.id.serverMessage), "Port invalide !");
        }
    }

    /**
     * <h3>Resets the server settings to default values</h3>
     * <p>
     * This method restores the IP address and port to the values defined in
     * <code>ServerDefaultConfig</code> and updates the corresponding input fields
     * in the UI.
     * </p>
     *
     * @param view the button clicked to trigger the reset
     */
    public void defaultServer(View view) {
        ClientManager myClientManager = ClientManager.getInstance();

        // Récupération des valeurs par défaut
        String defaultIp = ServerDefaultConfig.SERVER_HOST;
        int defaultPort = ServerDefaultConfig.SERVER_PORT;

        // Sauvegarde dans le ClientManager
        myClientManager.setIP(defaultIp);
        myClientManager.setPort(defaultPort);

        // Mise à jour de l'UI
        EditText ipField = findViewById(R.id.serverIpField);
        EditText portField = findViewById(R.id.serverPortField);

        ipField.setText(defaultIp);
        portField.setText(String.valueOf(defaultPort));

        HashMap<String, String> prefs = new HashMap<>();
        prefs.put("server_ip", ServerDefaultConfig.SERVER_HOST);
        prefs.put("server_port", String.valueOf(ServerDefaultConfig.SERVER_PORT));
        PrefsUtils.savePrefs(SERVER_PREFS, prefs, this);

        UiUtils.showMessage(findViewById(R.id.serverMessage), "Paramètres par défaut restaurés !");
    }


    // -------------------------
    // Validation helper methods
    // -------------------------


    /**
     * Validate the registration input fields.
     *
     * @param username The username entered by the user.
     * @param email The email entered by the user.
     * @param password The password entered by the user.
     * @param confirmPassword The confirmation of the password.
     * @param clientManager The client manager to check server connection.
     * @return Null if all inputs are valid, otherwise returns the error message.
     */
    private String validateRegistrationInput(String username, String email, String password, String confirmPassword, ClientManager clientManager) {
        if (!FormChecker.isValidEmail(email)) {
            return "Email invalide !";
        }
        if (!FormChecker.isValidUsername(username)) {
            return "Pseudo invalide !";
        }
        if (!FormChecker.passwordsMatch(password, confirmPassword)) {
            return "Les mots de passe ne correspondent pas !";
        }
        if (!FormChecker.isValidPassword(password)) {
            return "Mot de passe invalide !";
        }
        if (!clientManager.getKryoManager().isConnected()) {
            return "Connexion au serveur en cours...";
        }
        return null; // Everything is OK
    }



    // -------------------------
    // Intern logic
    // -------------------------
    //
    //
    //



    // -------------------------
    // Login execution
    // -------------------------

    /**
     * First phase when a user try to login.
     * We firstly try to connect to the server.
     */
    private void phaseConnectToServer() {
        loadingFragment.animateProgress(0f, 25f, INIT_WAITING_TIME, "Connecting to the server...", null, () -> {
            ClientManager.getInstance().getKryoManager().start();
            ClientManager.getInstance().getKryoManager().connect(
                clientManager.getIP(),
                clientManager.getPort(),
                () -> runOnUiThread(this::phaseConnectionSuccess),
                () -> runOnUiThread(this::phaseConnectionFailure)
            );
        });
    }

    /**
     * 2.a) if the client manage to connect
     */
    private void phaseConnectionSuccess() {
        loadingFragment.animateProgress(25f, 50f, INIT_WAITING_TIME, "Connected !", null, this::phaseSendCredentials);
    }

    /**
     * 2.b) if the client can't connect to the server
     */
    private void phaseConnectionFailure() {
        //ClientManager.getINSTANCE().closeSession();
        loadingFragment.setGradient(UiConfig.MEDIUM_RED, UiConfig.DARK_RED);
        loadingFragment.animateProgress(50f, 100f, INIT_WAITING_TIME, "Server not found !", null,
            () -> {
                loadingFragment.hide();
                UiUtils.showMessage(findViewById(R.id.loginMessage), "Server not found !");
            }
        );
    }

    /**
     * 3) Here we send the clients credentials.
     * The listeners will react and act accordingly to the server answer.
     */
    private void phaseSendCredentials() {
        loadingFragment.animateProgress(50f, 75f, INIT_WAITING_TIME, "Sending credentials !", null, () -> {
            new Thread(() -> {ClientManager.getInstance().login(email, password);}).start();
        });
    }


    // -------------------------
    // register execution
    // -------------------------

    /**
     * 1) We try to connect to the server in case of register
     */
    private void phaseConnectToServerForRegister() {
        loadingFragment.animateProgress(0f, 25f, INIT_WAITING_TIME, "Connecting to the server...", null, () -> {
            ClientManager.getInstance().getKryoManager().start();
            ClientManager.getInstance().getKryoManager().connect(
                clientManager.getIP(),
                clientManager.getPort(),
                () -> runOnUiThread(this::phaseConnectionSuccessForRegister),
                () -> runOnUiThread(this::phaseConnectionFailureForRegister)
            );
        });
    }

    /**
     * 2.a) If we manage to connect to the server
     */
    private void phaseConnectionSuccessForRegister() {
        loadingFragment.animateProgress(25f, 50f, INIT_WAITING_TIME, "Connected !", null, this::phaseSendRegisterCredentials);
    }

    /**
     * 2.b) If we can't connect to the server
     */
    private void phaseConnectionFailureForRegister() {
        //ClientManager.getINSTANCE().closeSession();
        loadingFragment.setGradient(UiConfig.MEDIUM_RED, UiConfig.DARK_RED);
        loadingFragment.animateProgress(50f, 100f, INIT_WAITING_TIME, "Server not found !", null,
            () -> {
                loadingFragment.hide();
                UiUtils.showMessage(findViewById(R.id.loginMessage), "Server not found !");
            }
        );
    }

    /**
     * 3) Sending the register credentials
     */
    private void phaseSendRegisterCredentials() {
        loadingFragment.animateProgress(50f, 75f, INIT_WAITING_TIME, "Sending registration data...", null, () -> {
            new Thread(() -> {
                ClientManager.getInstance().register(email, username, password, confirmPassword);
            }).start();
        });
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
                case LOGIN_SUCCESS:
                case REGISTER_SUCCESS:
                    ClientManager.getInstance().buildSession(authRequest);
                    loadingFragment.animateProgress(75f, 100f, INIT_WAITING_TIME, "Success !", null,
                        () -> {
                            ClientManager.getInstance().buildSession(authRequest);
                            RedirectUtils.simpleRedirectAndClearStack(this, HomeActivity.class);
                        }
                    );
                    return;
                case REGISTER_FAIL:
                    loadingFragment.setGradient(UiConfig.MEDIUM_RED, UiConfig.DARK_RED);
                    loadingFragment.animateProgress(75f, 100f, INIT_WAITING_TIME, "Error : " +message, null,
                        () -> {
                            //ClientManager.getINSTANCE().closeSession();
                            UiUtils.showMessage(findViewById(R.id.registerMessage), message);
                            loadingFragment.hide();
                        }
                    );
                    break;
                case LOGIN_FAIL:
                    loadingFragment.setGradient(UiConfig.MEDIUM_RED, UiConfig.DARK_RED);
                    loadingFragment.animateProgress(75f, 100f, INIT_WAITING_TIME, "Error : " + message, null,
                        () -> {
                            //ClientManager.getINSTANCE().closeSession();
                            UiUtils.showMessage(findViewById(R.id.loginMessage), message);
                            loadingFragment.hide();
                        }
                    );
                    break;

                default:
                    loadingFragment.setGradient(UiConfig.MEDIUM_RED, UiConfig.DARK_RED);
                    loadingFragment.animateProgress(75f, 100f, INIT_WAITING_TIME, "Unexpected responses !", null,
                        () -> {
                            //ClientManager.getINSTANCE().closeSession();
                            loadingFragment.hide();
                        }
                    );
            }
        }, true);
    }




    // -------------------------
    // UI Helper Methods
    // -------------------------
    // The following methods handle user interface interactions and visual components,
    // including password field visibility toggling and the setup of the ViewPager2
    // with bottom navigation dots for login/register screens.


    /**
     * Toggle the visibility of password input fields.
     *
     * This method is triggered when a "show/hide password" button is clicked.
     * Depending on which button is pressed, it will toggle the corresponding
     * EditText field between visible and hidden password states.
     *
     * Supported buttons and their target fields:
     * - R.id.togglePasswordVisibility → login password field
     * - R.id.toggleSignupPasswordVisibility → signup password field
     * - R.id.toggleConfirmPasswordVisibility → signup confirm password field
     *
     * @param view The button that was clicked.
     */
    public void togglePasswordClicked(View view) {
        int id = view.getId();
        if (id == R.id.togglePasswordVisibility) {
            UiUtils.togglePassword(findViewById(R.id.loginPassword), (ImageButton) view);
        } else if (id == R.id.toggleSignupPasswordVisibility) {
            UiUtils.togglePassword(findViewById(R.id.signupPassword), (ImageButton) view);
        } else if (id == R.id.toggleConfirmPasswordVisibility) {
            UiUtils.togglePassword(findViewById(R.id.signupConfirmPassword), (ImageButton) view);
        }
    }


    /**
     * Function that define the little dots on the bottom page and start the View2 for horizontal views.
     */
    private void setupViewPager(String loginError) {
        ViewPager2 viewPager = findViewById(R.id.viewPager);
        dotsLayout = findViewById(R.id.dotsLayout);

        this.adapter = new LauncherAdapter(this, loginError);
        viewPager.setAdapter(adapter);

        int pageCount = adapter.getItemCount();

        viewPager.setCurrentItem(1, false);

        viewPager.post(() -> {
            UiUtils.addBottomDots(this, dotsLayout, viewPager.getCurrentItem(), pageCount);
        });

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                UiUtils.addBottomDots(LoginActivity.this, dotsLayout, position, pageCount);
            }
        });
    }

    /**
     * This method init the loading fragment for the transitions animations
     */
    private void setupLoadingFragment(){
        loadingFragment = new LoadingFragment();
        getSupportFragmentManager().beginTransaction()
            .add(R.id.loadingOverlay, loadingFragment, "LOADING_FRAGMENT")
            .commit();
        FrameLayout overlay = findViewById(R.id.loadingOverlay);
        overlay.setOnTouchListener((v, event) -> overlay.getVisibility() == View.VISIBLE);
    }

    /**
     * This method is purely for UI, it's purpose is to hide the previously showed errors.
     */
    private void hideErrors(){
        RegisterFragment registerFragment = (RegisterFragment) adapter.getFragment(0);
        LoginFragment loginFragment = (LoginFragment) adapter.getFragment(1);
        ServerFragment serverFragment = (ServerFragment) adapter.getFragment(2);

        registerFragment.hideError();
        loginFragment.hideError();
        serverFragment.hideError();
    }






}
