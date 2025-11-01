package io.github.android.activity;

import static io.github.android.config.ClientDefaultConfig.CLIENT_SECURE;
import static io.github.android.config.ClientDefaultConfig.SERVER_PREFS;
import static io.github.android.utils.FormatUtils.isValidHostname;
import static io.github.android.utils.FormatUtils.isValidIPAddress;
import static io.github.android.utils.OtherUtils.initClientConfig;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import io.github.android.callback.main.FailConnectionCallback;
import io.github.android.callback.main.LoginAuthCallback;
import io.github.android.callback.main.RegisterAuthCallback;
import io.github.android.config.ServerDefaultConfig;
import io.github.android.gui.adapter.LauncherAdapter;
import io.github.android.gui.fragment.launcher.LoginFragment;
import io.github.android.gui.fragment.launcher.RegisterFragment;
import io.github.android.gui.fragment.launcher.ServerFragment;
import io.github.android.listeners.AuthClientListener;
import io.github.android.manager.ClientManager;
import io.github.android.utils.PrefsUtils;
import io.github.android.utils.UiUtils;
import io.github.core.client_engine.manager.KryoClientManager;
import io.github.core.client_engine.utils.FormChecker;
import io.github.fortheoil.R;

import androidx.viewpager2.widget.ViewPager2;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.util.Log;

import java.util.HashMap;


public class AndroidLauncher extends AppCompatActivity {

    private int[] layouts;
    private LinearLayout dotsLayout;
    private ClientManager clientManager;
    private LauncherAdapter adapter;

    private TextView registerMessage;
    private TextView loginMessage;
    private TextView serverMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);

        this.clientManager = ClientManager.getInstance();
        this.clientManager.setCurrentContext(this);

        AuthClientListener.getInstance(this, null).setCurrentActivity(this);

        // Vérifier si on a reçu un message d’erreur
        String loginError = getIntent().getStringExtra("login_error");

        setupViewPager(loginError);
        initClientConfig(this);
        //hideErrors();


    }


    @Override
    protected void onResume(){
        super.onResume();
        hideErrors();
    }

    private void hideErrors(){
        RegisterFragment registerFragment = (RegisterFragment) adapter.getFragment(0);
        LoginFragment loginFragment = (LoginFragment) adapter.getFragment(1);
        ServerFragment serverFragment = (ServerFragment) adapter.getFragment(2);

        registerFragment.hideError();
        loginFragment.hideError();
        serverFragment.hideError();
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

        String email = emailField.getText().toString().trim();
        String password = passwordField.getText().toString().trim();

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
        Log.d("AndroidLauncher", "Login requested: " + email);

        ClientManager clientManager = ClientManager.getInstance();
        KryoClientManager kryoManager = clientManager.getKryoManager();

        kryoManager.start();
        kryoManager.connect(
            clientManager.getIP(),
            clientManager.getPort(),
            // Here we create a callback that will be executed once we are connected
            new LoginAuthCallback(email, password),
            new FailConnectionCallback(errorField, "Error server unreachable...")
        );

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
        String username = usernameField.getText().toString().trim();
        String email = emailField.getText().toString().trim();
        String password = passwordField.getText().toString().trim();
        String confirmPassword = confirmPasswordField.getText().toString().trim();

        // Validate the inputs
        String errorMessage = validateRegistrationInput(username, email, password, confirmPassword, clientManager);
        if (errorMessage != null) {
            UiUtils.showMessage(errorField, errorMessage);
            return;
        }

        // Send to the server the Input
        Log.d("AndroidLauncher", "Register requested: " + email);


        ClientManager clientManager = ClientManager.getInstance();
        KryoClientManager kryoManager = clientManager.getKryoManager();

        kryoManager.start();
        kryoManager.connect(
            clientManager.getIP(),
            clientManager.getPort(),
            // Here we create a callback that will be executed once we are connected
            new RegisterAuthCallback(email, username, password, confirmPassword),
            new FailConnectionCallback(errorField,"Error server is unreachable ...")
        );

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
                UiUtils.addBottomDots(AndroidLauncher.this, dotsLayout, position, pageCount);
            }
        });
    }






}
