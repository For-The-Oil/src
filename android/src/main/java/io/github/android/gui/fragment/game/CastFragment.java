package io.github.android.gui.fragment.game;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import io.github.android.gui.WeaponStock;
import io.github.android.gui.adapter.WeaponStockAdapter;
import io.github.fortheoil.R;

public class CastFragment extends Fragment {

    private RecyclerView recyclerWeapons;
    private WeaponStockAdapter adapter;
    private final List<WeaponStock> weapons = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_cast, container, false);

        recyclerWeapons = view.findViewById(R.id.recyclerWeaponsStock);
        recyclerWeapons.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerWeapons.setItemAnimator(null);

        initFakeWeapons();

        adapter = new WeaponStockAdapter(weapons, weapon -> {
            // ACTION AU CLIC
            weapon.quantity--;
            adapter.notifyItemChanged(weapons.indexOf(weapon));
        });

        recyclerWeapons.setAdapter(adapter);

        return view;
    }

    private void initFakeWeapons() {
        weapons.add(new WeaponStock(
            "nuke",
            "Nuclear Missile",
            R.drawable.missile,
            3
        ));

        weapons.add(new WeaponStock(
            "ballistic",
            "Ballistic Missile",
            R.drawable.missile,
            12
        ));

        weapons.add(new WeaponStock(
            "emp",
            "EMP Bomb",
            R.drawable.hammer,
            5
        ));

        weapons.add(new WeaponStock(
            "artillery",
            "Heavy Artillery",
            R.drawable.helmet,
            20
        ));
    }
}
