package io.github.android.utils;

import static android.view.View.VISIBLE;

import android.content.Context;
import android.text.Html;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import io.github.android.activity.BaseActivity;
import io.github.fortheoil.R;
import io.github.shared.data.enums_types.EntityType;

/**
 * Classe utilitaire regroupant des fonctions réutilisables pour l'UI.
 * Exemples : toggle mot de passe, affichage de messages, etc.
 */
public final class UiUtils {

    // Empêche l'instanciation

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

    public static void addBottomDots(Context context, LinearLayout dotsLayout, int currentPage, int pageCount) {
        TextView[] dots = new TextView[pageCount];
        dotsLayout.removeAllViews();

        for (int i = 0; i < pageCount; i++) {
            dots[i] = new TextView(context);
            dots[i].setText(Html.fromHtml("&#8226;")); // point
            dots[i].setTextSize(35);
            dots[i].setTextColor(ContextCompat.getColor(context,
                (i == currentPage) ? R.color.dot_active : R.color.dot_inactive));
            dotsLayout.addView(dots[i]);
        }
    }


    public static void animateActivityChange(BaseActivity activity){
        activity.overridePendingTransition(R.anim.slide_in_bottom, R.anim.slide_out_top);
    }


    public static int mapEntityTypeToDrawable(EntityType type) {
        switch (type) {

            //Buildings
            case BASE: return R.drawable.base;
            case GARAGE: return R.drawable.garage;
            case FACTORY: return R.drawable.factory;
            case BARRACK: return R.drawable.barrack;
            case DERRICK: return R.drawable.derrick;
            case MINE: return R.drawable.mine;

            //Units
            case JEEP: return R.drawable.jeep;
            case BIKE: return R.drawable.bike;
            case TANK: return R.drawable.tank;

            default: return R.drawable.missing;
        }
    }


    // -----------------------------
    // Ajouter d'autres fonctions utiles ici…
    // -----------------------------
}
