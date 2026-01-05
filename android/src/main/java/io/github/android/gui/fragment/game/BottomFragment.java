package io.github.android.gui.fragment.game;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.artemis.ComponentMapper;
import com.artemis.World;

import io.github.core.client_engine.manager.SessionManager;
import io.github.core.data.ClientGame;
import io.github.fortheoil.R;
import io.github.shared.data.component.NetComponent;
import io.github.shared.data.enums_types.EntityType;
import io.github.shared.shared_engine.manager.EcsManager;

public class BottomFragment extends Fragment {

    // --- Interface pour que les sous-fragments reçoivent l'ID ---
    public interface SelectionAware {
        /**
         * Appelé quand le menu est ouvert via la sélection d'un bâtiment
         * @param netId L'ID réseau de l'entité, ou -1 si ouverture manuelle
         */
        void onEntitySelected(int netId);
    }

    private View root;
    private boolean isMenuOpen = false;
    private int menuMaxHeight;
    private Fragment currentFragment;
    private ImageButton activeButton = null;

    // Déclaration des fragments au niveau classe
    private Fragment unitsFragment, industryFragment, workforceFragment, defenseFragment, castFragment, mapFragment;

    // Déclaration des boutons au niveau classe
    private ImageButton btnUnits, btnIndustry, btnWorkforce, btnDefense, btnCast, btnMap;
    private ImageButton[] allMenuButtons;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        root = inflater.inflate(R.layout.game_bottom, container, false);
        menuMaxHeight = (int) (getResources().getDisplayMetrics().heightPixels * 0.4);

        initFragments();
        initButtons();

        return root;
    }

    private void initFragments() {
        unitsFragment = new UnitsFragment();
        industryFragment = new IndustryFragment();
        workforceFragment = new WorkforceFragment();
        defenseFragment = new DefenseFragment();
        castFragment = new CastFragment();
        mapFragment = new MapFragment();
    }

    private void initButtons() {
        btnUnits = root.findViewById(R.id.btnMenuUnits);
        btnIndustry = root.findViewById(R.id.btnMenuIndustry);
        btnWorkforce = root.findViewById(R.id.btnMenuWorkforce);
        btnDefense = root.findViewById(R.id.btnMenuDefense);
        btnCast = root.findViewById(R.id.btnMenuCast);
        btnMap = root.findViewById(R.id.btnMenuMap);

        allMenuButtons = new ImageButton[]{btnUnits, btnIndustry, btnWorkforce, btnDefense, btnCast, btnMap};

        // Configuration des clics manuels (ID = -1 par défaut)
        setupMenuButton(btnUnits, unitsFragment);
        setupMenuButton(btnIndustry, industryFragment);
        setupMenuButton(btnWorkforce, workforceFragment);
        setupMenuButton(btnDefense, defenseFragment);
        setupMenuButton(btnCast, castFragment);
        setupMenuButton(btnMap, mapFragment);
    }

    private void setupMenuButton(ImageButton button, Fragment menuFragment) {
        button.setOnClickListener(v -> {
            if (currentFragment == menuFragment) {
                // Même bouton → fermer le menu
                closeCurrentMenu();
            } else {
                // Nouveau bouton → ouvrir menu (ID -1 car clic manuel)
                openSpecificMenu(menuFragment, button, -1);
            }
        });
    }

    /**
     * Méthode centrale pour ouvrir un menu
     * @param fragment Le fragment à afficher
     * @param button Le bouton à mettre en surbrillance (peut être null)
     * @param entityNetId L'ID de l'entité sélectionnée (-1 si aucune)
     */
    private void openSpecificMenu(Fragment fragment, @Nullable ImageButton button, int entityNetId) {
        currentFragment = fragment;

        // 1. Gestion des boutons (visuel)
        if (activeButton != null) {
            activeButton.setAlpha(1f); // Reset l'ancien
        }
        activeButton = button;
        if (activeButton != null) {
            activeButton.setAlpha(0.6f); // Highlight le nouveau
        }

        // 2. Transmettre l'entité au fragment si possible
        if (fragment instanceof SelectionAware) {
            ((SelectionAware) fragment).onEntitySelected(entityNetId);
        } else if (entityNetId != -1) {
            // Fallback : Si le fragment n'implémente pas l'interface mais qu'on a un ID,
            // on peut tenter de passer un Bundle si le fragment n'est pas encore ajouté.
            Bundle args = new Bundle();
            args.putInt("selectedNetId", entityNetId);
            if (!fragment.isAdded()) {
                fragment.setArguments(args);
            }
        }

        // 3. Gestion de l'affichage (Animation ou Remplacement)
        if (isMenuOpen) {
            replaceMenuFragment(fragment);
        } else {
            openMenuFragment(fragment);
        }
    }

    // --- Logique métier pour la sélection d'entité ---

    public void showFragmentSelectBuilding(int netID) {
        World world = ClientGame.getInstance().getWorld();

        // Sécurité : si l'entité n'existe plus
        if (world == null) return;

        // --- CAS 1 : L'entité ne nous appartient pas ---
        if (EcsManager.findEntityByNetIdAndPlayer(world, netID, SessionManager.getInstance().getUuidClient()) == null) {
            // Fragment d'info générique (ennemi ou allié)
            Fragment infoFragment = new SelectedEntityFragment(netID);

            // On ouvre ce fragment sans associer de bouton de menu principal
            openSpecificMenu(infoFragment, null, netID);
        }
        // --- CAS 2 : L'entité nous appartient ---
        else {
            ComponentMapper<NetComponent> mapper = world.getMapper(NetComponent.class);
            int entityID = EcsManager.getIdByNetId(world, netID, mapper);

            if (entityID == -1) return;

            EntityType entityType = mapper.get(entityID).entityType;

            // Switch intelligent pour ouvrir le bon onglet du menu
            switch (entityType.getCategory()) {

                case Defense:
                    // Tourelles, murs -> Menu Défense
                    openSpecificMenu(defenseFragment, btnDefense, netID);
                    break;

                case Military:
                    // Casernes, usines de chars -> Menu Unités
                    openSpecificMenu(workforceFragment, btnWorkforce, netID);
                    break;

                case Industrial:
                    // Mines, extracteurs -> Menu Industrie
                    openSpecificMenu(industryFragment, btnIndustry, netID);
                    break;

                case Other:
                    // Maisons, civil -> Menu Workforce

                    break;

                default:
                    // Par défaut, infos génériques
                    openSpecificMenu(unitsFragment, btnUnits, netID);
                    break;
            }
        }
    }

    // --- Méthodes d'animation et de gestion de fragments ---

    private void replaceMenuFragment(Fragment menuFragment) {
        getChildFragmentManager()
            .beginTransaction()
            .replace(R.id.bottomMenuContainer, menuFragment)
            .commit();
    }

    private void openMenuFragment(Fragment menuFragment) {
        FrameLayout container = root.findViewById(R.id.bottomMenuContainer);
        container.setVisibility(View.VISIBLE);

        getChildFragmentManager()
            .beginTransaction()
            .replace(R.id.bottomMenuContainer, menuFragment)
            .commit();

        // Animation d'ouverture
        ValueAnimator animator = ValueAnimator.ofInt(0, menuMaxHeight);
        animator.addUpdateListener(animation -> {
            int value = (int) animation.getAnimatedValue();
            ViewGroup.LayoutParams params = container.getLayoutParams();
            params.height = value;
            container.setLayoutParams(params);

            for (ImageButton b : allMenuButtons) {
                b.setTranslationY(-value);
            }
        });
        animator.setDuration(250);
        animator.start();

        isMenuOpen = true;
    }

    private void closeCurrentMenu() {
        currentFragment = null;
        closeMenuFragment();
        if (activeButton != null) {
            activeButton.setAlpha(1f);
            activeButton = null;
        }
    }

    public void closeMenuFragment() {
        FrameLayout container = root.findViewById(R.id.bottomMenuContainer);

        // Animation de fermeture
        ValueAnimator animator = ValueAnimator.ofInt(container.getHeight(), 0);
        animator.addUpdateListener(animation -> {
            int value = (int) animation.getAnimatedValue();
            ViewGroup.LayoutParams params = container.getLayoutParams();
            params.height = value;
            container.setLayoutParams(params);

            for (ImageButton b : allMenuButtons) {
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
        resetButtonHighlights();
    }

    private void resetButtonHighlights() {
        for (ImageButton b : allMenuButtons) {
            if(b != null) b.setAlpha(1f);
        }
    }

    public void updateUI(){
        // TODO: Mettre à jour les fragments enfants si nécessaire
        if (isMenuOpen && currentFragment != null) {
            // Exemple : si le fragment actuel a une méthode update()
            // if (currentFragment instanceof Updatable) ((Updatable)currentFragment).update();
        }
    }
}
