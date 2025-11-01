package io.github.android.gui.adapter;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import io.github.android.gui.fragment.launcher.LoginFragment;
import io.github.android.gui.fragment.launcher.RegisterFragment;
import io.github.android.gui.fragment.launcher.ServerFragment;

public class LauncherAdapter extends FragmentStateAdapter {

    private final Fragment[] fragments;

    public LauncherAdapter(@NonNull FragmentActivity fragmentActivity, String loginError) {
        super(fragmentActivity);

        LoginFragment loginFragment = new LoginFragment();
        if (loginError != null) {
            Bundle args = new Bundle();
            args.putString("login_error", loginError);
            loginFragment.setArguments(args);
        }


        fragments = new Fragment[] {
            new RegisterFragment(),
            loginFragment,
            new ServerFragment()
        };
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return fragments[position];
    }

    @Override
    public int getItemCount() {
        return fragments.length;
    }

    public Fragment getFragment(int position) {
        return fragments[position];
    }
}
