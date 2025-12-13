package io.github.android.gui.fragment.game;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import io.github.fortheoil.R;

public class BottomFragment extends Fragment {

    private View root;
    private boolean isMenuOpen = false;
    private int menuMaxHeight;
    private Fragment currentFragment;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        root = inflater.inflate(R.layout.game_bottom, container, false);

        menuMaxHeight = (int) (getResources().getDisplayMetrics().heightPixels * 0.25);

        // Créer tous les fragments du menu
        Fragment unitsFragment = new UnitsFragment();
        Fragment buildingsFragment = new UnitsFragment();
        Fragment resourcesFragment = new UnitsFragment();
        Fragment defenseFragment = new UnitsFragment();
        Fragment missilesFragment = new UnitsFragment();
        Fragment mapFragment = new UnitsFragment();

        // Lier les boutons aux fragments
        setupMenuButton(root.findViewById(R.id.btnMenu1), unitsFragment);
        setupMenuButton(root.findViewById(R.id.btnMenu2), buildingsFragment);
        setupMenuButton(root.findViewById(R.id.btnMenu3), resourcesFragment);
        setupMenuButton(root.findViewById(R.id.btnMenu4), defenseFragment);
        setupMenuButton(root.findViewById(R.id.btnMenu5), missilesFragment);
        setupMenuButton(root.findViewById(R.id.btnMenu6), mapFragment);

        return root;
    }

    private void setupMenuButton(ImageButton button, Fragment menuFragment) {
        button.setOnClickListener(v -> {
            if (isMenuOpen) {
                currentFragment=null;
                closeMenuFragment();
            } else {
                currentFragment=menuFragment;
                openMenuFragment(menuFragment);
            }
        });
    }

    private void openMenuFragment(Fragment menuFragment) {
        FrameLayout container = root.findViewById(R.id.bottomMenuContainer);
        container.setVisibility(View.VISIBLE);

        // Remplacer le fragment
        getChildFragmentManager()
            .beginTransaction()
            .replace(R.id.bottomMenuContainer, menuFragment)
            .commit();

        ImageButton btnMenu1 = root.findViewById(R.id.btnMenu1);
        ImageButton btnMenu2 = root.findViewById(R.id.btnMenu2);
        ImageButton btnMenu3 = root.findViewById(R.id.btnMenu3);
        ImageButton btnMenu4 = root.findViewById(R.id.btnMenu4);
        ImageButton btnMenu5 = root.findViewById(R.id.btnMenu5);
        ImageButton btnMenu6 = root.findViewById(R.id.btnMenu6);

        ImageButton[] buttons = {btnMenu1, btnMenu2, btnMenu3, btnMenu4, btnMenu5, btnMenu6};

        // Animation de hauteur + déplacement des boutons
        ValueAnimator animator = ValueAnimator.ofInt(0, menuMaxHeight);
        animator.addUpdateListener(animation -> {
            int value = (int) animation.getAnimatedValue();
            ViewGroup.LayoutParams params = container.getLayoutParams();
            params.height = value;
            container.setLayoutParams(params);

            // Déplacer les boutons vers le haut
            for (ImageButton b : buttons) {
                b.setTranslationY(-value); // déplace vers le haut de "value" pixels
            }
        });
        animator.setDuration(250);
        animator.start();

        isMenuOpen = true;
    }

    private void closeMenuFragment() {
        FrameLayout container = root.findViewById(R.id.bottomMenuContainer);

        ImageButton btnMenu1 = root.findViewById(R.id.btnMenu1);
        ImageButton btnMenu2 = root.findViewById(R.id.btnMenu2);
        ImageButton btnMenu3 = root.findViewById(R.id.btnMenu3);
        ImageButton btnMenu4 = root.findViewById(R.id.btnMenu4);
        ImageButton btnMenu5 = root.findViewById(R.id.btnMenu5);
        ImageButton btnMenu6 = root.findViewById(R.id.btnMenu6);

        ImageButton[] buttons = {btnMenu1, btnMenu2, btnMenu3, btnMenu4, btnMenu5, btnMenu6};

        ValueAnimator animator = ValueAnimator.ofInt(container.getHeight(), 0);
        animator.addUpdateListener(animation -> {
            int value = (int) animation.getAnimatedValue();
            ViewGroup.LayoutParams params = container.getLayoutParams();
            params.height = value;
            container.setLayoutParams(params);

            // Déplacer les boutons vers le bas
            for (ImageButton b : buttons) {
                b.setTranslationY(-value);
            }
        });
        animator.setDuration(250);
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                container.setVisibility(View.GONE);
                Fragment f = getChildFragmentManager().findFragmentById(R.id.bottomMenuContainer);
                if (f != null) {
                    getChildFragmentManager().beginTransaction().remove(f).commit();
                }
            }
        });
        animator.start();

        isMenuOpen = false;
    }

}
