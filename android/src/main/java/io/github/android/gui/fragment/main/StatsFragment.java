package io.github.android.gui.fragment.main;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Arrays;

import io.github.android.gui.adapter.UnitCardAdapter;
import io.github.shared.data.enumsTypes.EntityType;
import io.github.fortheoil.R;

public class StatsFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflater la vue
        View root = inflater.inflate(R.layout.second_activity_stats, container, false);

        // Initialiser la RecyclerView
        RecyclerView unitRecyclerView = root.findViewById(R.id.unitRecyclerView);
        unitRecyclerView.setLayoutManager(
            new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false)
        );
        UnitCardAdapter adapter = new UnitCardAdapter(Arrays.asList(EntityType.values()));
        unitRecyclerView.setAdapter(adapter);

        return root; // On retourne la vue à la fin
    }
}
