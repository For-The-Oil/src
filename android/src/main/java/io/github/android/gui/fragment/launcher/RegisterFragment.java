package io.github.android.gui.fragment.launcher;

import androidx.fragment.app.Fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import io.github.fortheoil.R;

public class RegisterFragment extends Fragment {

    private TextView registerMessage;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.main_activity_register, container, false);
        registerMessage = root.findViewById(R.id.registerMessage);
        return root;
    }

    public void showError(String message) {
        if (registerMessage != null) {
            registerMessage.setText(message);
            registerMessage.setVisibility(View.VISIBLE);
        }
    }

    public void hideError(){
        if (registerMessage != null) {
            registerMessage.setText("");
            registerMessage.setVisibility(View.GONE);
        }
    }

}
