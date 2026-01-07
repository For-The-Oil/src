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
 * Fragment gérant l'interface utilisateur pour la catégorie "Main-d'œuvre" (Militaire).
 * <p>
 * Ce fragment est une implémentation de {@link BaseDeckFragment} spécialisée pour :
 * <ul>
 * <li>Afficher et permettre la construction d'unités/bâtiments militaires.</li>
 * <li>Lister les infrastructures militaires existantes appartenant au joueur.</li>
 * <li>Fournir des outils de gestion (rotation, suppression) pour ces entités.</li>
 * </ul>
 */
public class WorkforceFragment extends BaseDeckFragment {

    /** Tag utilisé pour les logs de débogage. */
    private static final String TAG = "WORKFORCE_FRAGMENT";

    /** RecyclerView affichant les bâtiments militaires déjà déployés. */
    private RecyclerView recyclerExisting;

    /**
     * Gonfle le layout XML associé au fragment "Workforce".
     */
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_workforce, container, false);
    }

    /**
     * Initialise les composants de l'interface et configure la liste des bâtiments existants.
     * <p>
     * Appelle {@code super.onViewCreated} pour initialiser la logique de construction
     * commune à tous les decks.
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialisation de la section des bâtiments existants (généralement en bas de l'UI)
        recyclerExisting = view.findViewById(R.id.recyclerSection4);
        if (recyclerExisting != null) {
            recyclerExisting.setLayoutManager(new LinearLayoutManager(getContext()));
            refreshExistingBuildings();
        }
    }

    /**
     * Interroge le moteur de jeu pour récupérer les bâtiments militaires du joueur.
     * <p>
     * Cette méthode récupère le {@link GraphicsSyncSystem} pour extraire les IDs réseau
     * des entités militaires et les affiche via un {@link ExistingBuildingAdapter}.
     */
    private void refreshExistingBuildings() {
        GraphicsSyncSystem gfx = ClientGame.getInstance().getWorld().getSystem(GraphicsSyncSystem.class);
        if (gfx != null) {
            // Récupération des entités filtrées par le type militaire/workforce
            ArrayList<Integer> buildings = gfx.getEntityBuildingMilitary();

            Log.d(TAG, "Refresh - Nb bâtiments militaires trouvés = " + (buildings != null ? buildings.size() : 0));

            if (buildings != null) {
                // Initialisation de l'adapter avec un callback vers onExistingBuildingSelected
                ExistingBuildingAdapter adapter = new ExistingBuildingAdapter(buildings, id -> {
                    Log.d(TAG, "Entité sélectionnée (Workforce): " + id);
                    onExistingBuildingSelected(id);
                });
                recyclerExisting.setAdapter(adapter);
            }
        } else {
            Log.e(TAG, "GraphicsSyncSystem introuvable !");
        }
    }

    /**
     * Retourne la catégorie de deck associée à ce fragment.
     * @return {@link DeckCardCategory#Military}
     */
    @Override
    protected DeckCardCategory getCategory() {
        return DeckCardCategory.Military;
    }

    /**
     * Met à jour la liste des bâtiments militaires après une action (ex: suppression).
     */
    @Override
    protected void refreshExistingList() {
        refreshExistingBuildings();
    }
}
