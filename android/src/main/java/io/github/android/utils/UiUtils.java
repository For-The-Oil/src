package io.github.android.utils;

import static android.view.View.VISIBLE;

import android.app.Activity;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import io.github.fortheoil.R;

/**
 * Classe utilitaire regroupant des fonctions réutilisables pour l'UI.
 * Exemples : toggle mot de passe, affichage de messages, etc.
 */
public final class UiUtils {

    // Empêche l'instanciation
    private UiUtils() {}

    public static void togglePassword(EditText field, ImageButton button) {
        boolean visible = field.getTransformationMethod() instanceof HideReturnsTransformationMethod;
        if (visible) {
            field.setTransformationMethod(PasswordTransformationMethod.getInstance());
            button.setImageResource(android.R.drawable.ic_menu_view);
        } else {
            field.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
            button.setImageResource(android.R.drawable.ic_menu_close_clear_cancel);
        }
        field.setSelection(field.getText().length());
    }

    // -----------------------------
    // Affiche un message dans un TextView et le rend visible
    // -----------------------------
    public static void showMessage(TextView messageView, String message) {
        if (messageView != null) {
            messageView.setText(message);
            messageView.setVisibility(VISIBLE);
        }
    }

    // -----------------------------
    // Cache un TextView
    // -----------------------------
    public static void hideMessage(TextView messageView) {
        if (messageView != null) {
            messageView.setVisibility(View.GONE);
        }
    }

    // -----------------------------
    // Exemple de fonction utilitaire pour EditText : vider le texte
    // -----------------------------
    public static void clearField(EditText editText) {
        if (editText != null) {
            editText.setText("");
        }
    }

    public static void addBottomDots(Activity activity, LinearLayout dotsLayout, int currentPage, int[] layouts) {
        ImageView[] dots = new ImageView[layouts.length];
        dotsLayout.removeAllViews();

        for (int i = 0; i < layouts.length; i++) {
            dots[i] = new ImageView(activity); // ici le contexte est passé
            dots[i].setImageResource(i == currentPage ? android.R.drawable.presence_online : android.R.drawable.presence_invisible);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(30, 30);
            params.setMargins(10,0,10,0);
            dotsLayout.addView(dots[i], params);
        }
    }




    // -----------------------------
    // Ajouter d'autres fonctions utiles ici…
    // -----------------------------
}
