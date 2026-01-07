package io.github.android.gui.fragment.game;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import io.github.android.gui.adapter.ExistingBuildingAdapter;
import io.github.core.data.ClientGame;
import io.github.core.game_engine.system.GraphicsSyncSystem;
import io.github.fortheoil.R;
import io.github.shared.data.enums_types.DeckCardCategory;

/**
 * Fragment gérant l'interface utilisateur pour la catégorie "Défense".
 * <p>
 * Cette implémentation de {@link BaseDeckFragment} est dédiée aux structures protectrices.
 * Elle permet au joueur de :
 * <ul>
 * <li>Sélectionner de nouveaux bâtiments défensifs (tours, murs, etc.) dans le deck.</li>
 * <li>Visualiser et gérer les systèmes de défense déjà actifs sur le terrain.</li>
 * <li>Accéder aux commandes de rotation et de suppression pour ces entités spécifiques.</li>
 * </ul>
 */
public class DefenseFragment extends BaseDeckFragment {

    /** Tag d'identification pour les journaux de débogage (Logcat). */
    private static final String TAG = "DEFENSE_FRAGMENT";

    /** Liste visuelle (RecyclerView) affichant les bâtiments de défense existants. */
    private RecyclerView recyclerExisting;

    /**
     * Initialise la vue du fragment à partir du layout XML spécialisé pour la défense.
     */
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_defense, container, false);
    }

    /**
     * Configure les composants UI après la création de la vue.
     * <p>
     * Initialise le {@link RecyclerView} pour les bâtiments existants et déclenche
     * le premier chargement des données.
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        // Initialisation de la logique de base (Deck, Boutons d'action)
        super.onViewCreated(view, savedInstanceState);

        // Liaison avec le composant graphique de la liste des bâtiments existants
        recyclerExisting = view.findViewById(R.id.recyclerSection4);

        if (recyclerExisting != null) {
            recyclerExisting.setLayoutManager(new LinearLayoutManager(getContext()));
            refreshExistingBuildings();
        }
    }

    /**
     * Synchronise la liste des bâtiments de défense avec l'état actuel du moteur de jeu.
     * <p>
     * Utilise le {@link GraphicsSyncSystem} pour filtrer les entités de type défense.
     * En cas de succès, met à jour l'{@link ExistingBuildingAdapter}.
     */
    private void refreshExistingBuildings() {
        GraphicsSyncSystem gfx = ClientGame.getInstance().getWorld().getSystem(GraphicsSyncSystem.class);
        if (gfx != null) {
            // Extraction des IDs réseau des entités défensives
            ArrayList<Integer> buildings = gfx.getEntityBuildingDefense();

            Log.d(TAG, "Refresh - Nb bâtiments défense trouvés = " + (buildings != null ? buildings.size() : 0));

            if (buildings != null) {
                // Création de l'adaptateur avec redirection vers la logique de sélection parente
                ExistingBuildingAdapter adapter = new ExistingBuildingAdapter(buildings, id -> {
                    Log.d(TAG, "Entité sélectionnée (Defense): " + id);
                    onExistingBuildingSelected(id);
                });
                recyclerExisting.setAdapter(adapter);
            }
        } else {
            Log.e(TAG, "GraphicsSyncSystem introuvable !");
        }
    }

    /**
     * Définit la catégorie de cartes à charger pour ce fragment.
     * @return {@link DeckCardCategory#Defense}
     */
    @Override
    protected DeckCardCategory getCategory() {
        return DeckCardCategory.Defense;
    }

    /**
     * Rafraîchit la liste des bâtiments de défense.
     * Cette méthode est appelée par la classe mère lors d'un changement d'état (ex: destruction).
     */
    @Override
    protected void refreshExistingList() {
        refreshExistingBuildings();
    }
}
