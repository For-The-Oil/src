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
import java.util.List;

import io.github.android.gui.adapter.ExistingBuildingAdapter;
import io.github.core.data.ClientGame;
import io.github.core.game_engine.system.GraphicsSyncSystem;
import io.github.fortheoil.R;
import io.github.shared.data.enums_types.DeckCardCategory;

/**
 * Fragment gérant l'interface utilisateur pour la catégorie "Industrie".
 * <p>
 * Ce fragment étend {@link BaseDeckFragment} pour :
 * <ul>
 * <li>Afficher les cartes de construction de type {@link DeckCardCategory#Industrial}.</li>
 * <li>Lister les bâtiments industriels déjà existants appartenant au joueur.</li>
 * <li>Permettre l'interaction (sélection/suppression) avec les infrastructures en place.</li>
 * </ul>
 */
public class IndustryFragment extends BaseDeckFragment {

    /** * RecyclerView affichant la liste des bâtiments industriels
     * actuellement déployés sur la carte.
     */
    private RecyclerView recyclerExisting;

    /**
     * Gonfle le layout spécifique à la section industrie.
     */
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_industry, container, false);
    }

    /**
     * Initialise les composants de l'interface après la création de la vue.
     * <p>
     * Configure le {@link RecyclerView} pour les bâtiments existants et
     * appelle l'initialisation parente pour le deck de construction.
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        // Initialisation de la logique commune (boutons d'action, deck de cartes)
        super.onViewCreated(view, savedInstanceState);

        // Configuration du recycler pour les bâtiments déjà construits (Section 4 du layout)
        recyclerExisting = view.findViewById(R.id.recyclerSection4);
        recyclerExisting.setLayoutManager(new LinearLayoutManager(getContext()));

        refreshExistingBuildings();
    }

    /**
     * Récupère la liste des bâtiments industriels du joueur via le système
     * de synchronisation graphique et met à jour l'affichage.
     * <p>
     * Cette méthode utilise le {@link GraphicsSyncSystem} pour filtrer les entités
     * de type industriel présentes dans le monde.
     */
    // Dans IndustryFragment.java
    private void refreshExistingBuildings() {
        GraphicsSyncSystem gfx = ClientGame.getInstance().getWorld().getSystem(GraphicsSyncSystem.class);
        if (gfx == null) return;

        ArrayList<Integer> myndustrialBuildings = gfx.getEntityBuildingIndustry();

        // On initialise l'adapter du parent
        existingAdapter = new ExistingBuildingAdapter(myndustrialBuildings, id -> {
            // Quand on clique dans la liste, on récupère le NetID et on propage
            int netId = gfx.getNetIdFromEntity(id);
            if (netId != -1) {
                onEntitySelected(netId);
            }
        });

        recyclerExisting.setAdapter(existingAdapter);

        // --- SYNCHRONISATION IMMÉDIATE ---
        // Si un bâtiment était déjà sélectionné sur la map avant l'ouverture du menu
        int netIdToSelect = (targetedEntityNetId != -1) ? targetedEntityNetId : pendingSelectedNetId;
        if (netIdToSelect != -1) {
            existingAdapter.setSelectedNetId(netIdToSelect);
            onExistingBuildingSelected(targetedEntityNetId);

            int pos = existingAdapter.getPositionOfNetId(netIdToSelect);
            if (pos != -1) {
                recyclerExisting.scrollToPosition(pos);
            }
        }
    }

    /**
     * Surcharge de la méthode parente déclenchée après une suppression réussie.
     * Permet de rafraîchir visuellement la liste des bâtiments restants.
     */
    @Override
    protected void refreshExistingList() {
        refreshExistingBuildings();
    }

    /**
     * Définit la catégorie de ce fragment comme étant Industrielle.
     * @return {@link DeckCardCategory#Industrial}
     */
    @Override
    protected DeckCardCategory getCategory() {
        return DeckCardCategory.Industrial;
    }

    @Override
    public void updateDynamicUI() {
        GraphicsSyncSystem gfx = ClientGame.getInstance().getWorld().getSystem(GraphicsSyncSystem.class);
        if (gfx == null || existingAdapter == null) return;

        // On récupère la liste actuelle des entités depuis le moteur
        ArrayList<Integer> currentEntities = gfx.getEntityBuildingIndustry();

        // On met à jour la liste interne de l'adapter (il faut ajouter une méthode setEntities dans l'adapter)
        existingAdapter.setEntityIds(currentEntities);
    }

}
