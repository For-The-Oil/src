package io.github.android.gui.animation;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.view.View;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class AnimatorBar {

    /** Étape d’animation générique */
    public static class Step {
        public final View target;             // Vue à animer
        public final String property;         // Propriété à animer ("progress", "translationX", etc.)
        public final float[] values;          // Valeurs de l’animation
        public final long durationMs;         // Durée
        public final String message;          // Message à afficher
        public final Runnable onStart;        // Callback début
        public final Runnable onEnd;          // Callback fin
        public final Interpolator interpolator;

        public Step(View target,
                    String property,
                    float[] values,
                    long durationMs,
                    String message,
                    Runnable onStart,
                    Runnable onEnd,
                    Interpolator interpolator) {
            this.target = target;
            this.property = property;
            this.values = values;
            this.durationMs = durationMs;
            this.message = message;
            this.onStart = onStart;
            this.onEnd = onEnd;
            this.interpolator = interpolator;
        }
    }

    private final ProgressBar progressBar;
    private final TextView textView;
    private final List<Step> currentSteps = new ArrayList<>();
    private final Queue<List<Step>> queue = new LinkedList<>();

    private Runnable onFinish;
    private Interpolator defaultInterpolator = new LinearInterpolator();
    private boolean isRunning = false;

    public AnimatorBar(ProgressBar progressBar, TextView textView) {
        this.progressBar = progressBar;
        this.textView = textView;
    }

    public AnimatorBar addStep(Step step) {
        currentSteps.add(step);
        return this;
    }

    public AnimatorBar onFinish(Runnable onFinish) {
        this.onFinish = onFinish;
        return this;
    }

    /** Lance la séquence courante et la met dans la file */
    public void run() {
        if (currentSteps.isEmpty()) {
            if (onFinish != null) onFinish.run();
            return;
        }
        queue.add(new ArrayList<>(currentSteps));
        currentSteps.clear();

        if (!isRunning) {
            playNext();
        }
    }

    private void playNext() {
        List<Step> steps = queue.poll();
        if (steps == null) {
            isRunning = false;
            return;
        }
        isRunning = true;

        List<Animator> anims = new ArrayList<>();
        for (Step s : steps) {
            ObjectAnimator anim;

            // Cas particulier : ProgressBar → utiliser ofInt
            if ("progress".equals(s.property) && s.target instanceof ProgressBar) {
                int[] intValues = new int[s.values.length];
                for (int i = 0; i < s.values.length; i++) {
                    intValues[i] = (int) s.values[i];
                }
                anim = ObjectAnimator.ofInt(s.target, s.property, intValues);
            } else {
                // Cas général → ofFloat
                anim = ObjectAnimator.ofFloat(s.target, s.property, s.values);
            }

            anim.setDuration(s.durationMs);
            anim.setInterpolator(s.interpolator != null ? s.interpolator : defaultInterpolator);

            anim.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationStart(Animator animation) {
                    if (s.message != null && textView != null) {
                        textView.setText(s.message);
                    }
                    if (s.onStart != null) s.onStart.run();
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    if (s.onEnd != null) s.onEnd.run();
                }
            });

            anims.add(anim);
        }

        AnimatorSet set = new AnimatorSet();
        set.playSequentially(anims);
        set.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if (onFinish != null) onFinish.run();
                playNext(); // enchaîne la séquence suivante si elle existe
            }
        });
        set.start();
    }

}
