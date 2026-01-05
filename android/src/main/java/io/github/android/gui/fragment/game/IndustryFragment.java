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

public class IndustryFragment extends BaseDeckFragment {

    private RecyclerView recyclerExisting;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // On gonfle la vue
        return inflater.inflate(R.layout.fragment_industry, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        // Appelle onViewCreated du parent pour initialiser la partie "Deck/Construction"
        super.onViewCreated(view, savedInstanceState);

        // Initialisation de la section 4 (Bâtiments existants)
        recyclerExisting = view.findViewById(R.id.recyclerSection4);
        recyclerExisting.setLayoutManager(new LinearLayoutManager(getContext()));

        refreshExistingBuildings();
    }

    private void refreshExistingBuildings() {
        GraphicsSyncSystem gfx = ClientGame.getInstance().getWorld().getSystem(GraphicsSyncSystem.class);
        if (gfx != null) {
            ArrayList<Integer> myndustrialBuildings = gfx.getEntityBuildingIndustry();
            Log.d("INDUSTRY FRAGMENT","Test nb elems = "+myndustrialBuildings.size());
            ExistingBuildingAdapter adapter = new ExistingBuildingAdapter(myndustrialBuildings, id -> {
                onExistingBuildingSelected(id);
            });
            recyclerExisting.setAdapter(adapter);
        }
    }

    @Override
    protected DeckCardCategory getCategory() {
        return DeckCardCategory.Industrial;
    }
}
