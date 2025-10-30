package io.github.android;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import io.github.android.gui.MyPagerAdapter;
import io.github.android.manager.ClientManager;
import io.github.android.manager.KryoClientManager;
import io.github.android.utils.UiUtils;
import io.github.core.client_engine.utils.FormChecker;
import io.github.fortheoil.R;

import androidx.viewpager2.widget.ViewPager2;

import android.widget.EditText;
import android.widget.ImageButton;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Map;

public class AndroidLauncher extends AppCompatActivity {

    private int[] layouts;
    private LinearLayout dotsLayout;
    private ClientManager clientManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);

        ViewPager2 viewPager = findViewById(R.id.viewPager);
        dotsLayout = findViewById(R.id.dotsLayout);
        layouts = new int[]{R.layout.main_activity_login, R.layout.main_activity_register};

        MyPagerAdapter adapter = new MyPagerAdapter(this, layouts);
        viewPager.setAdapter(adapter);

        UiUtils.addBottomDots(this, dotsLayout, viewPager.getCurrentItem(), layouts);
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                UiUtils.addBottomDots(AndroidLauncher.this, dotsLayout, position, layouts);
            }
        });

        // --- Initialisation du ClientManager ---
        clientManager = ClientManager.getInstance();
        clientManager.connectToServer("10.0.2.2", 54555); // IP émulateur vers localhost


    }

    public void login(View view) {
        EditText emailField = findViewById(R.id.loginEmail);
        EditText passwordField = findViewById(R.id.loginPassword);
        CheckBox autoLoginBox = findViewById(R.id.autoLogin);

        String email = emailField.getText().toString().trim();
        String password = passwordField.getText().toString().trim();
        boolean remember = autoLoginBox.isChecked();

        Log.d("AndroidLauncher", "Login requested: " + email);

        if (!FormChecker.isValidEmail(email)) {
            UiUtils.showMessage(findViewById(R.id.loginMessage), "Email invalide !");
            return;
        }

        clientManager.setConnectionListener(new KryoClientManager.ConnectionListener() {
            @Override
            public void onConnected() {
                Log.d("AndroidLauncher", "Connected callback fired, sending login...");
                clientManager.login(email, password);
            }

            @Override
            public void onDisconnected() { }

            @Override
            public void onMessageReceived(Object message) { }
        });

    }

    public void register(View view) {
        EditText usernameField = findViewById(R.id.signupUsername);
        EditText emailField = findViewById(R.id.signupEmail);
        EditText passwordField = findViewById(R.id.signupPassword);
        EditText confirmPasswordField = findViewById(R.id.signupConfirmPassword);
        CheckBox autoLoginBox = findViewById(R.id.autoLoginRegister);
        TextView errorField = findViewById(R.id.registerMessage);

        String username = usernameField.getText().toString().trim();
        String email = emailField.getText().toString().trim();
        String password = passwordField.getText().toString().trim();
        String confirmPassword = confirmPasswordField.getText().toString().trim();
        boolean remember = autoLoginBox.isChecked();

        Log.d("AndroidLauncher", "Register requested: " + email);

        if (!FormChecker.isValidEmail(email)) {
            UiUtils.showMessage(errorField, "Email invalide !");
            return;
        }
        if (!FormChecker.isValidUsername(username)) {
            UiUtils.showMessage(errorField, "Pseudo invalide !");
            return;
        }
        if (!FormChecker.passwordsMatch(password, confirmPassword)) {
            UiUtils.showMessage(errorField, "Les mots de passe ne correspondent pas !");
            return;
        }
        if (!FormChecker.isValidPassword(password)) {
            UiUtils.showMessage(errorField, "Mot de passe invalide !");
            return;
        }

        clientManager.register(email, username, password);
    }

    public void togglePasswordClicked(View view) {
        if (view.getId() == R.id.togglePasswordVisibility) {
            EditText loginPassword = findViewById(R.id.loginPassword);
            UiUtils.togglePassword(loginPassword, (ImageButton) view);
        } else if (view.getId() == R.id.toggleSignupPasswordVisibility) {
            EditText signupPassword = findViewById(R.id.signupPassword);
            UiUtils.togglePassword(signupPassword, (ImageButton) view);
        } else if (view.getId() == R.id.toggleConfirmPasswordVisibility) {
            EditText signupConfirm = findViewById(R.id.signupConfirmPassword);
            UiUtils.togglePassword(signupConfirm, (ImageButton) view);
        }
    }
}
