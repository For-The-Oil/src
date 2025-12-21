package io.github.android.gui.fragment.game;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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
    private ImageButton activeButton = null;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        root = inflater.inflate(R.layout.game_bottom, container, false);

        menuMaxHeight = (int) (getResources().getDisplayMetrics().heightPixels * 0.4);

        // Créer tous les fragments du menu
        Fragment unitsFragment = new UnitsFragment();
        Fragment industryFragment = new IndustryFragment();
        Fragment workforceFragment = new WorkforceFragment();
        Fragment defenseFragment = new DefenseFragment();
        Fragment castFragment = new CastFragment();
        Fragment mapFragment = new MapFragment();

        // Lier les boutons aux fragments
        setupMenuButton(root.findViewById(R.id.btnMenuUnits), unitsFragment);
        setupMenuButton(root.findViewById(R.id.btnMenuIndustry), industryFragment);
        setupMenuButton(root.findViewById(R.id.btnMenuWorkforce), workforceFragment);
        setupMenuButton(root.findViewById(R.id.btnMenuDefense), defenseFragment);
        setupMenuButton(root.findViewById(R.id.btnMenuCast), castFragment);
        setupMenuButton(root.findViewById(R.id.btnMenuMap), mapFragment);

        return root;
    }

    private void setupMenuButton(ImageButton button, Fragment menuFragment) {
        button.setOnClickListener(v -> {
            if (currentFragment == menuFragment) {
                // Même bouton → fermer le menu
                currentFragment = null;
                closeMenuFragment();

                // Réinitialiser le bouton actif
                if (activeButton != null) {
                    activeButton.setAlpha(1f); // état normal
                    activeButton = null;
                }
            } else {
                // Nouveau bouton → changer fragment
                currentFragment = menuFragment;

                // Mettre à jour le bouton actif
                if (activeButton != null) {
                    activeButton.setAlpha(1f); // bouton précédent normal
                }
                activeButton = button;
                activeButton.setAlpha(0.6f); // bouton actif plus foncé

                if (isMenuOpen) {
                    // Menu déjà ouvert → juste remplacer fragment
                    replaceMenuFragment(menuFragment);
                } else {
                    // Menu fermé → ouvrir avec animation
                    openMenuFragment(menuFragment);
                }
            }
        });
    }



    private void replaceMenuFragment(Fragment menuFragment) {
        FrameLayout container = root.findViewById(R.id.bottomMenuContainer);
        getChildFragmentManager()
            .beginTransaction()
            .replace(R.id.bottomMenuContainer, menuFragment)
            .commit();
    }


    private void highlightButton(ImageButton activeButton) {
        // Remet tous les boutons à l'état normal
        ImageButton[] buttons = {
            root.findViewById(R.id.btnMenuUnits),
            root.findViewById(R.id.btnMenuIndustry),
            root.findViewById(R.id.btnMenuWorkforce),
            root.findViewById(R.id.btnMenuDefense),
            root.findViewById(R.id.btnMenuCast),
            root.findViewById(R.id.btnMenuMap)
        };

        for (ImageButton b : buttons) {
            b.setAlpha(1f); // normal
        }

        // Bouton actif plus foncé
        activeButton.setAlpha(0.6f);
    }

    private void resetButtonHighlights() {
        ImageButton[] buttons = {
            root.findViewById(R.id.btnMenuUnits),
            root.findViewById(R.id.btnMenuIndustry),
            root.findViewById(R.id.btnMenuWorkforce),
            root.findViewById(R.id.btnMenuDefense),
            root.findViewById(R.id.btnMenuCast),
            root.findViewById(R.id.btnMenuMap)
        };
        for (ImageButton b : buttons) {
            b.setAlpha(1f);
        }
    }




    private void openMenuFragment(Fragment menuFragment) {
        FrameLayout container = root.findViewById(R.id.bottomMenuContainer);
        container.setVisibility(View.VISIBLE);

        // Remplacer le fragment
        getChildFragmentManager()
            .beginTransaction()
            .replace(R.id.bottomMenuContainer, menuFragment)
            .commit();

        ImageButton btnMenu1 = root.findViewById(R.id.btnMenuUnits);
        ImageButton btnMenu2 = root.findViewById(R.id.btnMenuIndustry);
        ImageButton btnMenu3 = root.findViewById(R.id.btnMenuWorkforce);
        ImageButton btnMenu4 = root.findViewById(R.id.btnMenuDefense);
        ImageButton btnMenu5 = root.findViewById(R.id.btnMenuCast);
        ImageButton btnMenu6 = root.findViewById(R.id.btnMenuMap);

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

        ImageButton btnMenu1 = root.findViewById(R.id.btnMenuUnits);
        ImageButton btnMenu2 = root.findViewById(R.id.btnMenuIndustry);
        ImageButton btnMenu3 = root.findViewById(R.id.btnMenuWorkforce);
        ImageButton btnMenu4 = root.findViewById(R.id.btnMenuDefense);
        ImageButton btnMenu5 = root.findViewById(R.id.btnMenuCast);
        ImageButton btnMenu6 = root.findViewById(R.id.btnMenuMap);

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
