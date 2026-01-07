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
import com.badlogic.gdx.Gdx;

import io.github.core.client_engine.manager.SessionManager;
import io.github.core.data.ClientGame;
import io.github.fortheoil.R;
import io.github.shared.data.component.NetComponent;
import io.github.shared.data.enums_types.EntityType;
import io.github.shared.shared_engine.manager.EcsManager;

/**
 * Fragment gérant la barre de navigation inférieure et le menu contextuel du jeu.
 * <p>
 * Cette classe agit comme un orchestrateur pour :
 * <ul>
 * <li>Afficher les différents onglets de construction (Unités, Industrie, etc.).</li>
 * <li>Gérer l'animation d'ouverture/fermeture du tiroir (sliding menu).</li>
 * <li>Intercepter la sélection d'entités sur la carte pour ouvrir l'onglet approprié.</li>
 * <li>Gérer l'état visuel (surbrillance) des boutons de navigation.</li>
 * </ul>
 */
public class BottomFragment extends Fragment {

    /**
     * Interface permettant aux fragments contenus de réagir à la sélection d'une entité.
     * Les fragments implémentant cette interface recevront l'ID réseau de l'entité
     * dès leur ouverture.
     */
    public interface SelectionAware {
        /**
         * Appelé quand le menu est ouvert via la sélection d'un bâtiment.
         * @param netId L'ID réseau de l'entité, ou -1 si l'ouverture est manuelle.
         */
        void onEntitySelected(int netId);
    }

    /** Vue racine du fragment (game_bottom). */
    private View root;

    /** Indique si le tiroir du menu est actuellement déployé. */
    private boolean isMenuOpen = false;

    /** Hauteur maximale du menu calculée dynamiquement (40% de l'écran). */
    private int menuMaxHeight;

    /** Référence vers le fragment actuellement affiché dans le conteneur. */
    private Fragment currentFragment;

    /** Bouton actuellement sélectionné (celui qui est en surbrillance). */
    private ImageButton activeButton = null;

    // --- Instances des fragments de menu ---
    private Fragment unitsFragment, industryFragment, workforceFragment, defenseFragment, castFragment, mapFragment;

    // --- Composants UI ---
    private ImageButton btnUnits, btnIndustry, btnWorkforce, btnDefense, btnCast, btnMap;

    /** Tableau regroupant tous les boutons pour faciliter les réinitialisations groupées. */
    private ImageButton[] allMenuButtons;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.game_bottom, container, false);
        // Calcul de la hauteur max : 40% de la hauteur totale de l'écran
        menuMaxHeight = (int) (getResources().getDisplayMetrics().heightPixels * 0.4);

        initFragments();
        initButtons();

        return root;
    }

    /**
     * Initialise les instances des sous-fragments pour éviter de les recréer à chaque clic.
     */
    private void initFragments() {
        unitsFragment = new UnitsFragment();
        industryFragment = new IndustryFragment();
        workforceFragment = new WorkforceFragment();
        defenseFragment = new DefenseFragment();
        castFragment = new CastFragment();
        mapFragment = new MapFragment();
    }

    /**
     * Initialise les boutons de menu, les listeners et synchronise l'état visuel initial.
     */
    private void initButtons() {
        btnUnits = root.findViewById(R.id.btnMenuUnits);
        btnIndustry = root.findViewById(R.id.btnMenuIndustry);
        btnWorkforce = root.findViewById(R.id.btnMenuWorkforce);
        btnDefense = root.findViewById(R.id.btnMenuDefense);
        btnCast = root.findViewById(R.id.btnMenuCast);
        btnMap = root.findViewById(R.id.btnMenuMap);

        allMenuButtons = new ImageButton[]{btnUnits, btnIndustry, btnWorkforce, btnDefense, btnCast, btnMap};

        setupMenuButton(btnUnits, unitsFragment);
        setupMenuButton(btnIndustry, industryFragment);
        setupMenuButton(btnWorkforce, workforceFragment);
        setupMenuButton(btnDefense, defenseFragment);
        setupMenuButton(btnCast, castFragment);
        setupMenuButton(btnMap, mapFragment);

        updateActiveButtonState();
        syncActiveButtonState();
    }

    /**
     * Met à jour la référence du bouton actif en fonction du fragment courant.
     */
    public void updateActiveButtonState() {
        if (currentFragment == null) return;

        ImageButton targetButton = null;
        if (currentFragment == unitsFragment) targetButton = btnUnits;
        else if (currentFragment == industryFragment) targetButton = btnIndustry;
        else if (currentFragment == workforceFragment) targetButton = btnWorkforce;
        else if (currentFragment == defenseFragment) targetButton = btnDefense;
        else if (currentFragment == castFragment) targetButton = btnCast;
        else if (currentFragment == mapFragment) targetButton = btnMap;

        if (targetButton != null) {
            if (activeButton != null) activeButton.setAlpha(1f);
            activeButton = targetButton;
            activeButton.setAlpha(0.6f);
        }
    }

    /**
     * Applique visuellement l'opacité sur le bouton actif pour simuler une sélection.
     */
    private void syncActiveButtonState() {
        if (currentFragment == null) return;

        ImageButton target = null;
        if (currentFragment == unitsFragment) target = btnUnits;
        else if (currentFragment == industryFragment) target = btnIndustry;
        else if (currentFragment == workforceFragment) target = btnWorkforce;
        else if (currentFragment == defenseFragment) target = btnDefense;
        else if (currentFragment == castFragment) target = btnCast;
        else if (currentFragment == mapFragment) target = btnMap;

        if (target != null) {
            if (activeButton != null) activeButton.setAlpha(1f);
            activeButton = target;
            activeButton.setAlpha(0.6f);
        }
    }

    /**
     * Configure le comportement au clic d'un bouton de la barre de navigation.
     * @param button Le bouton physique cliqué.
     * @param menuFragment Le fragment de destination associé au bouton.
     */
    private void setupMenuButton(ImageButton button, Fragment menuFragment) {
        button.setOnClickListener(v -> {
            if (currentFragment == menuFragment) {
                closeCurrentMenu();
            } else {
                openSpecificMenu(menuFragment, button, -1);
            }
        });
    }

    /**
     * Logique centrale pour ouvrir ou remplacer un fragment dans le tiroir coulissant.
     * * @param fragment Le fragment à afficher.
     * @param button Le bouton à mettre en surbrillance.
     * @param entityNetId L'ID de l'entité sélectionnée (-1 si aucune).
     */
    private void openSpecificMenu(Fragment fragment, @Nullable ImageButton button, int entityNetId) {
        currentFragment = fragment;

        if (activeButton != null) activeButton.setAlpha(1f);
        activeButton = button;
        if (activeButton != null) activeButton.setAlpha(0.6f);

        // Transmission de l'ID via interface ou Bundle
        if (fragment instanceof SelectionAware) {
            ((SelectionAware) fragment).onEntitySelected(entityNetId);
        } else if (entityNetId != -1) {
            Bundle args = new Bundle();
            args.putInt("selectedNetId", entityNetId);
            if (!fragment.isAdded()) {
                fragment.setArguments(args);
            }
        }

        if (isMenuOpen) {
            replaceMenuFragment(fragment);
        } else {
            openMenuFragment(fragment);
        }
    }

    /**
     * Analyse une entité sélectionnée dans le monde pour ouvrir l'onglet correspondant.
     * Gère les entités alliées/ennemies (SelectedEntityFragment) et les nôtres (switch catégorie).
     * * @param netID ID réseau de l'entité sélectionnée.
     */
    public void showFragmentSelectBuilding(int netID) {
        World world = ClientGame.getInstance().getWorld();
        if (world == null) return;

        // Vérification appartenance
        if (EcsManager.findEntityByNetIdAndPlayer(world, netID, SessionManager.getInstance().getUuidClient()) == null) {
            Fragment infoFragment = new SelectedEntityFragment(netID);
            openSpecificMenu(infoFragment, null, netID);
        } else {
            ComponentMapper<NetComponent> mapper = world.getMapper(NetComponent.class);
            int entityID = EcsManager.getIdByNetId(world, netID, mapper);
            if (entityID == -1) return;

            EntityType entityType = mapper.get(entityID).entityType;

            // Routage intelligent vers le bon fragment
            switch (entityType.getCategory()) {
                case Defense:
                    openSpecificMenu(defenseFragment, btnDefense, netID);
                    break;
                case Military:
                    openSpecificMenu(workforceFragment, btnWorkforce, netID);
                    break;
                case Industrial:
                    openSpecificMenu(industryFragment, btnIndustry, netID);
                    break;
                case Other:
                    // Logique pour les bâtiments "Other" (non implémentée)
                    break;
                default:
                    openSpecificMenu(unitsFragment, btnUnits, netID);
                    break;
            }
        }
    }

    /**
     * Remplace le contenu du conteneur sans rejouer l'animation de montée.
     */
    private void replaceMenuFragment(Fragment menuFragment) {
        getChildFragmentManager()
            .beginTransaction()
            .replace(R.id.bottomMenuContainer, menuFragment)
            .commit();
    }

    /**
     * Déploie le menu avec une animation de translation vers le haut.
     * Fait également monter les boutons de navigation.
     */
    private void openMenuFragment(Fragment menuFragment) {
        FrameLayout container = root.findViewById(R.id.bottomMenuContainer);
        container.setVisibility(View.VISIBLE);

        getChildFragmentManager()
            .beginTransaction()
            .replace(R.id.bottomMenuContainer, menuFragment)
            .commit();

        ValueAnimator animator = ValueAnimator.ofInt(0, menuMaxHeight);
        animator.addUpdateListener(animation -> {
            int value = (int) animation.getAnimatedValue();
            ViewGroup.LayoutParams params = container.getLayoutParams();
            params.height = value;
            container.setLayoutParams(params);

            // Synchronisation de la position des boutons avec le tiroir
            for (ImageButton b : allMenuButtons) {
                b.setTranslationY(-value);
            }
        });
        animator.setDuration(250);
        animator.start();

        isMenuOpen = true;
    }

    /**
     * Réinitialise l'état local avant de lancer la fermeture.
     */
    public void closeCurrentMenu() {
        currentFragment = null;
        closeMenuFragment();
        if (activeButton != null) {
            activeButton.setAlpha(1f);
            activeButton = null;
        }
    }

    /**
     * Réduit le menu avec une animation de descente et retire le fragment actif.
     */
    public void closeMenuFragment() {
        FrameLayout container = root.findViewById(R.id.bottomMenuContainer);

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

    /**
     * Réinitialise l'opacité (Alpha) de tous les boutons du menu.
     */
    private void resetButtonHighlights() {
        for (ImageButton b : allMenuButtons) {
            if(b != null) b.setAlpha(1f);
        }
    }

    /**
     * Permet la mise à jour périodique de l'UI des fragments enfants si nécessaire.
     */
    public void updateUI() {
        // On vérifie que le menu est ouvert ET que le fragment est bien un BaseDeckFragment
        if (isMenuOpen && currentFragment instanceof BaseDeckFragment) {
            ((BaseDeckFragment) currentFragment).updateDynamicUI();
        } else {
            // Optionnel : Logique pour MapFragment ou autre fragment si nécessaire
            Gdx.app.log("UPDATE UI", "Fragment actuel n'est pas un BaseDeckFragment, skip update.");
        }
    }
}
