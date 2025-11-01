package io.github.android.gui.fragment.launcher;

import static io.github.android.config.ClientDefaultConfig.SERVER_PREFS;

import androidx.fragment.app.Fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import java.util.HashMap;

import io.github.android.utils.PrefsUtils;
import io.github.fortheoil.R;

public class LoginFragment extends Fragment {

    private TextView loginMessage;
    private CheckBox autoLoginCheckBox;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.main_activity_login, container, false);

        loginMessage = root.findViewById(R.id.loginMessage);
        autoLoginCheckBox = root.findViewById(R.id.autoLogin);



        HashMap<String, String> myMap = PrefsUtils.loadPrefs(SERVER_PREFS, requireContext());
        boolean autoLoginEnabled = Boolean.parseBoolean(myMap.getOrDefault("auto_login", "false"));
        autoLoginCheckBox.setChecked(autoLoginEnabled);

        Bundle args = getArguments();
        if (args != null) {
            String error = args.getString("login_error");
            if (error != null) {
                showError(error);
            }
        }

        return root;
    }

    public void showError(String message) {
        if (loginMessage != null) {
            loginMessage.setText(message);
            loginMessage.setVisibility(View.VISIBLE);
        }
    }

    public void hideError(){
        if (loginMessage != null) {
            loginMessage.setText("");
            loginMessage.setVisibility(View.GONE);
        }
    }

    public TextView getErrorField() {
        return loginMessage;
    }

}
