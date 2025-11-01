package io.github.android.utils;

import static androidx.core.content.ContextCompat.startActivity;

import android.app.Activity;
import android.content.Intent;

import androidx.annotation.Nullable;

import io.github.android.activity.BaseActivity;
import io.github.android.activity.HomeActivity;
import io.github.android.activity.LoginActivity;

public final class RedirectUtils {

    public static BaseActivity withRedirectToLauncher(BaseActivity activity, @Nullable String error) {
        Intent intent = new Intent(activity, LoginActivity.class);
        if (error != null && !error.isEmpty()) {
            intent.putExtra("login_error", error);
        }
        activity.startActivity(intent);
        return activity;
    }

    public static BaseActivity withRedirectToMainMenu(BaseActivity activity) {
        Intent intent = new Intent(activity, HomeActivity.class);
        activity.startActivity(intent);
        return activity;
    }


}
