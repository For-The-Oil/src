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
    private static final String TAG = "DEFENSE_FRAGMENT";
    private RecyclerView recyclerExisting;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_defense, container, false);
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

        ArrayList<Integer> buildings = gfx.getEntityBuildingDefense();

        existingAdapter = new ExistingBuildingAdapter(buildings, id -> {
            int netId = gfx.getNetIdFromEntity(id);
            if (netId != -1) onEntitySelected(netId);
        });

        recyclerExisting.setAdapter(existingAdapter);

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

        // On utilise la méthode défense spécifique
        existingAdapter.setEntityIds(gfx.getEntityBuildingDefense());
    }

    protected RecyclerView getExistingRecycler() {
        return recyclerExisting;
    }

    @Override
    protected DeckCardCategory getCategory() { return DeckCardCategory.Defense; }

    @Override
    protected void refreshExistingList() { refreshExistingBuildings(); }
}
