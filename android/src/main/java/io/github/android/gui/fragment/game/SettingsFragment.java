package io.github.android.gui.fragment.game;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import io.github.android.activity.GameActivity;
import io.github.fortheoil.R;

public class SettingsFragment extends Fragment {

    private View settingsPanel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        settingsPanel = inflater.inflate(R.layout.game_settings, container, false);

        setupButtons();
        return settingsPanel;
    }

    private void setupButtons() {
        Button btnReturn = settingsPanel.findViewById(R.id.btnReturn);
        Button btnQuit = settingsPanel.findViewById(R.id.btnQuit);



        btnReturn.setOnClickListener(v ->
            ((GameActivity) requireActivity()).closeSettingsFragment()
        );

        btnQuit.setOnClickListener(v -> {
                GameActivity activity = (GameActivity) requireActivity();
                activity.closeSettingsFragment();
                activity.quitGame();
            });
    }


}
