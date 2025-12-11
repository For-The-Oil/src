package io.github.android.gui.fragment.launcher;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.ClipDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import io.github.android.config.UiConfig;
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
    public void onViewCreated(View view, Bundle savedInstanceState) {
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
    public void animateProgress(float start, float end, long duration, String message, Runnable onStart, Runnable onEnd) {
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
     * Affiche le fragment en rendant visible son conteneur.
     */
    public void show() {
        if (getActivity() != null) {
            View container = getActivity().findViewById(R.id.loadingOverlay);
            if (container != null) container.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Masque le fragment en rendant invisible son conteneur.
     */
    public void hide() {
        if (getActivity() != null) {
            View container = getActivity().findViewById(R.id.loadingOverlay);
            if (container != null) container.setVisibility(View.GONE);
        }
    }

    public void setColor(@ColorInt int color) {
       splashProgress.setProgressTintList(ColorStateList.valueOf(color));
    }

    public void setGradient(int startColor, int endColor) {
        Drawable drawable = splashProgress.getProgressDrawable();

        if (drawable instanceof LayerDrawable) {
            LayerDrawable layerDrawable = (LayerDrawable) drawable;

            GradientDrawable gradient = new GradientDrawable(
                GradientDrawable.Orientation.LEFT_RIGHT,
                new int[]{startColor, endColor}
            );
            gradient.setCornerRadius(40f);

            ClipDrawable clip = new ClipDrawable(gradient, Gravity.LEFT, ClipDrawable.HORIZONTAL);

            layerDrawable.setDrawableByLayerId(android.R.id.progress, clip);
        }
    }



    public void setDefaultGradient(){
        setGradient(UiConfig.LIGHT_GREEN, UiConfig.DARK_GREEN);
    }


    public ProgressBar getSplashProgress() {
        return splashProgress;
    }

    public void setSplashProgress(ProgressBar splashProgress) {
        this.splashProgress = splashProgress;
    }
}
