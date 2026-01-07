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
    private static final String TAG = "WORKFORCE_FRAGMENT";
    private RecyclerView recyclerExisting;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_workforce, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        recyclerExisting = view.findViewById(R.id.recyclerSection4);
        if (recyclerExisting != null) {
            recyclerExisting.setLayoutManager(new LinearLayoutManager(getContext()));
            refreshExistingBuildings();
        }
    }

    private void refreshExistingBuildings() {
        GraphicsSyncSystem gfx = ClientGame.getInstance().getWorld().getSystem(GraphicsSyncSystem.class);
        if (gfx == null) return;

        ArrayList<Integer> buildings = gfx.getEntityBuildingMilitary();

        existingAdapter = new ExistingBuildingAdapter(buildings, id -> {
            // Traduction ID local -> NetID
            int netId = gfx.getNetIdFromEntity(id);
            if (netId != -1) onEntitySelected(netId);
        });

        recyclerExisting.setAdapter(existingAdapter);

        // Synchronisation immédiate (clic map ou en attente)
        int netIdToSelect = (targetedEntityNetId != -1) ? targetedEntityNetId : pendingSelectedNetId;
        if (netIdToSelect != -1) {
            existingAdapter.setSelectedNetId(netIdToSelect);
            onExistingBuildingSelected(netIdToSelect);
            int pos = existingAdapter.getPositionOfNetId(netIdToSelect);
            if (pos != -1) recyclerExisting.scrollToPosition(pos);
        }
    }

    @Override
    public void updateDynamicUI() {
        GraphicsSyncSystem gfx = ClientGame.getInstance().getWorld().getSystem(GraphicsSyncSystem.class);
        if (gfx == null || existingAdapter == null) return;

        // On utilise la méthode militaire spécifique
        existingAdapter.setEntityIds(gfx.getEntityBuildingMilitary());
    }

    @Override
    protected DeckCardCategory getCategory() { return DeckCardCategory.Military; }

    @Override
    protected void refreshExistingList() { refreshExistingBuildings(); }
}
