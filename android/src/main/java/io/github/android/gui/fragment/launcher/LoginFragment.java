package io.github.android.gui.fragment.launcher;

import androidx.fragment.app.Fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import io.github.fortheoil.R;

public class LoginFragment extends Fragment {

    private TextView loginMessage;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.main_activity_login, container, false);
        loginMessage = root.findViewById(R.id.loginMessage);
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

}
