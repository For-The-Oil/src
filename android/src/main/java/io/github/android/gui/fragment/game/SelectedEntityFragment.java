package io.github.android.gui.fragment.game;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.artemis.ComponentMapper;
import com.artemis.Entity;
import com.artemis.World;

import io.github.core.data.ClientGame;
import io.github.fortheoil.R;
import io.github.shared.data.component.LifeComponent;
import io.github.shared.data.component.NetComponent;
import io.github.shared.shared_engine.manager.EcsManager;

public class SelectedEntityFragment extends Fragment {

    private int netID;
    private int entityId; // L'ID local dans le World Artemis

    public SelectedEntityFragment(int netID) {
        this.netID = netID;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_select_building, container, false);

        // 1. Récupérer le World et l'Entité
        World world = ClientGame.getInstance().getWorld();
        entityId = EcsManager.getIdByNetId(world, netID, world.getMapper(NetComponent.class));

        if (entityId != -1) {
            setupUI(root, world, entityId);
        }

        return root;
    }

    private void setupUI(View root, World world, int eId) {
        ComponentMapper<LifeComponent> mLife = world.getMapper(LifeComponent.class);


    }



}
