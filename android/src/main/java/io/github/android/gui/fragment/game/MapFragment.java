package io.github.android.gui.fragment.game;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import io.github.android.gui.PixelMapView;
import io.github.core.data.ClientGame;
import io.github.fortheoil.R;

public class MapFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_map, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        PixelMapView mapView = view.findViewById(R.id.pixelMapView);
        if (!ClientGame.isInstanceNull()) {
            mapView.setMap(ClientGame.getInstance().getMap());
        }

        mapView.setOnTouchListener((v, event) -> true); // tout touch est consommé
    }



}
