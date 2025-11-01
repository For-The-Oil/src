package io.github.android.gui.fragment.launcher;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import io.github.fortheoil.R;
import io.github.android.gui.animation.AnimatorBar;

public class LoadingFragment extends Fragment {

    private TextView splashText;
    private ProgressBar splashProgress;
    private AnimatorBar bar;

    public LoadingFragment() {
        super(R.layout.splash_screen);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        splashText = view.findViewById(R.id.splashText);
        splashProgress = view.findViewById(R.id.splashProgress);
        bar = new AnimatorBar(splashProgress, splashText);
    }

    /**
     * Met à jour le texte affiché sur le splash screen.
     */
    public void showMessage(String message) {
        if (splashText != null) {
            splashText.setText(message);
        }
    }

    /**
     * Anime la barre de progression avec AnimatorBar.
     *
     * @param start Valeur de départ (0-100)
     * @param end Valeur finale (0-100)
     * @param duration Durée de l'animation en ms
     * @param message Message optionnel à afficher pendant l'animation
     * @param onStart Runnable à exécuter au début de l'animation (nullable)
     * @param onEnd Runnable à exécuter à la fin de l'animation (nullable)
     */
    public void animateProgress(float start, float end, long duration, String message,
                                @Nullable Runnable onStart, @Nullable Runnable onEnd) {
        if (bar != null && splashProgress != null) {
            bar.addStep(new AnimatorBar.Step(
                splashProgress,
                "progress",
                new float[]{start, end},
                duration,
                message,
                onStart,
                onEnd,
                null
            )).run();
        }
    }

    /**
     * Affiche le fragment (overlay).
     */
    public void show(View parent) {
        if (getView() != null) {
            getView().setVisibility(View.VISIBLE);
        }
    }

    /**
     * Masque le fragment (overlay).
     */
    public void hide() {
        if (getView() != null) {
            getView().setVisibility(View.GONE);
        }
    }
}
