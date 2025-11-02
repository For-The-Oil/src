package io.github.android.utils;

import static androidx.core.content.ContextCompat.startActivity;

import android.app.Activity;
import android.content.Intent;

import androidx.annotation.Nullable;

import java.util.Map;

import io.github.android.activity.BaseActivity;

public final class RedirectUtils {

    /**
     * Redirection simple (ne vide pas la pile).
     */
    public static void simpleRedirect(BaseActivity activity, Class<? extends Activity> target) {
        Intent intent = new Intent(activity, target);
        activity.startActivity(intent);
    }


    /**
     * Redirection simple avec un extra (clé/valeur).
     */
    public static void simpleRedirect(BaseActivity activity, Class<? extends Activity> target, String key, String value) {
        Intent intent = new Intent(activity, target);
        intent.putExtra(key, value);
        activity.startActivity(intent);
    }



    /**
     * Redirection simple avec plusieurs extras optionnels.
     */
    public static void simpleRedirect(BaseActivity activity, Class<? extends Activity> target, @Nullable Map<String, String> extras) {
        if (activity == null) return;

        Intent intent = new Intent(activity, target);

        if (extras != null) {
            for (Map.Entry<String, String> entry : extras.entrySet()) {
                intent.putExtra(entry.getKey(), entry.getValue());
            }
        }

        activity.startActivity(intent);
    }

    /**
     * Redirection complète (vide la pile et appelle safeKill()).
     */
    public static void simpleRedirectAndClearStack(BaseActivity activity, Class<? extends Activity> target) {
        activity.safeKill();
        Intent intent = new Intent(activity, target);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        activity.startActivity(intent);
        activity.finish();
    }


    /**
     * Redirection complète avec en extra une paire (clé/valeur).
     */
    public static void simpleRedirectAndClearStack(BaseActivity activity, Class<? extends Activity> target, String key, String value) {
        activity.safeKill();
        Intent intent = new Intent(activity, target);
        intent.putExtra(key, value);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        activity.startActivity(intent);
        activity.finish();
    }


    /**
     * Redirection complète en vidant la pile d'activités,
     * en appelant safeKill(), et en passant plusieurs extras optionnels.
     */
    public static void simpleRedirectAndClearStack(BaseActivity activity, Class<? extends Activity> target, @Nullable Map<String, String> extras) {
        if (activity == null) return;

        activity.safeKill();

        Intent intent = new Intent(activity, target);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        if (extras != null) {
            for (Map.Entry<String, String> entry : extras.entrySet()) {
                intent.putExtra(entry.getKey(), entry.getValue());
            }
        }

        activity.startActivity(intent);
        activity.finish();
    }




}
