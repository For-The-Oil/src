package io.github.android.gui.fragment.game;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import io.github.fortheoil.R;
import io.github.shared.data.enums_types.DeckCardCategory;

public class DefenseFragment extends BaseDeckFragment {

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(io.github.fortheoil.R.layout.fragment_defense, container, false);
    }
    @Override
    protected DeckCardCategory getCategory() {
        return DeckCardCategory.Defense;
    }




}
