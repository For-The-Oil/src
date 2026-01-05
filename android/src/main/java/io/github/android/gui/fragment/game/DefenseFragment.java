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

public class DefenseFragment extends BaseDeckFragment {

    private static final String TAG = "DEFENSE_FRAGMENT";
    private RecyclerView recyclerExisting;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_defense, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerExisting = view.findViewById(R.id.recyclerSection4); // Vérifiez cet ID dans fragment_defense
        if (recyclerExisting != null) {
            recyclerExisting.setLayoutManager(new LinearLayoutManager(getContext()));
            refreshExistingBuildings();
        }
    }

    private void refreshExistingBuildings() {
        GraphicsSyncSystem gfx = ClientGame.getInstance().getWorld().getSystem(GraphicsSyncSystem.class);
        if (gfx != null) {
            // Note : J'utilise ici la méthode supposée pour la défense
            ArrayList<Integer> buildings = gfx.getEntityBuildingDefense();

            Log.d(TAG, "Refresh - Nb bâtiments défense trouvés = " + (buildings != null ? buildings.size() : 0));

            if (buildings != null) {
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

    @Override
    protected DeckCardCategory getCategory() {
        return DeckCardCategory.Defense;
    }
}
