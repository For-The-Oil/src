package io.github.android.gui.fragment.main;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import io.github.android.activity.HomeActivity;
import io.github.android.manager.MatchMakingManager;
import io.github.fortheoil.R;

public class MatchMakingFragment extends Fragment {

    private HomeActivity activity;
    private TextView timerText;
    private Button cancelButton;

    private Handler timerHandler = new Handler();
    private long startTime = 0;

    private final Runnable timerRunnable = new Runnable() {
        @Override
        public void run() {
            long millis = System.currentTimeMillis() - startTime;
            int seconds = (int) (millis / 1000);
            int minutes = seconds / 60;
            seconds %= 60;

            if (timerText != null)timerText.setText(String.format("%02d:%02d", minutes, seconds));

            timerHandler.postDelayed(this, 500);
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.second_activity_matchmaking, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        activity = (HomeActivity) getActivity();
        if (activity == null) return;

        timerText = view.findViewById(R.id.matchmakingTimer);
        cancelButton = view.findViewById(R.id.matchmakingCancelButton);

        cancelButton.setOnClickListener(v -> cancelMatchmaking());

        // L’overlay est caché par défaut
        view.setVisibility(View.GONE);
    }

    // -------------------------
    // Méthodes publiques
    // -------------------------

    /** Affiche l’overlay et lance le timer */
    public void show() {
        View v = getView();
        if (v != null) {
            v.setVisibility(View.VISIBLE);
            startTimer();
        } else {
            Log.e("MatchMakingFragment", "View not yet created!");
        }
    }


    /** Cache l’overlay et arrête le timer */
    public void hide() {
        if (getView() != null) getView().setVisibility(View.GONE);
        stopTimer();
    }

    // -------------------------
    // Gestion du timer
    // -------------------------

    private void startTimer() {
        startTime = System.currentTimeMillis();
        timerHandler.post(timerRunnable);
    }

    private void stopTimer() {
        timerHandler.removeCallbacks(timerRunnable);
    }

    // -------------------------
    // Action du bouton Cancel
    // -------------------------

    private void cancelMatchmaking() {
        stopTimer();

        // Stop la recherche côté client
        MatchMakingManager.getInstance().cancelMatchmaking();

        // Cache l’overlay
        hide();

        // Optionnel : afficher un petit loading si nécessaire
        if (activity != null && activity.loadingFragment != null) {
            activity.loadingFragment.show();
            activity.loadingFragment.animateProgress(0f, 100f, 500, "Cancelling...", null, () -> {
                activity.loadingFragment.hide();
            });
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        stopTimer();
    }
}
