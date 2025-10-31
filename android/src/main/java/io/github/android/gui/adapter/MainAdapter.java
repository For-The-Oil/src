package io.github.android.gui.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import io.github.android.gui.fragment.main.DeckFragment;
import io.github.android.gui.fragment.main.MainPageFragment;
import io.github.android.gui.fragment.main.StatsFragment;

public class MainAdapter extends FragmentStateAdapter {

    public MainAdapter(@NonNull FragmentActivity fa) {
        super(fa);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0: return new DeckFragment();
            case 1: return new MainPageFragment();
            case 2: return new StatsFragment();
            default: return new MainPageFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 3;
    }
}
