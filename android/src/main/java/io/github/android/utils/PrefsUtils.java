package io.github.android.utils;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.HashMap;
import java.util.Map;

import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

import java.io.IOException;
import java.security.GeneralSecurityException;
public final class PrefsUtils {


    public static void savePrefs(String file , HashMap<String,String> prefs, Context context){
        SharedPreferences myPrefs = context.getSharedPreferences(file, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = myPrefs.edit();

        for (String key: prefs.keySet()) {
            editor.putString(key, prefs.get(key));
        }
        editor.apply();
    }

    public static HashMap<String, String> loadPrefs(String file, Context context) {
        SharedPreferences myPrefs = context.getSharedPreferences(file, Context.MODE_PRIVATE);
        Map<String, ?> allEntries = myPrefs.getAll();
        HashMap<String, String> prefs = new HashMap<>();
        for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
            prefs.put(entry.getKey(), String.valueOf(entry.getValue()));
        }
        return prefs;
    }

    private static SharedPreferences getEncryptedPrefs(String file, Context context)
        throws GeneralSecurityException, IOException {

        // Crée ou récupère une clé maître stockée dans le Keystore
        MasterKey masterKey = new MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build();

        return EncryptedSharedPreferences.create(
            context,
            file, // nom du fichier de préférences
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        );
    }

    public static void saveEncryptedPrefs(String file, HashMap<String, String> prefs, Context context) {
        try {
            SharedPreferences myPrefs = getEncryptedPrefs(file, context);
            SharedPreferences.Editor editor = myPrefs.edit();

            for (Map.Entry<String, String> entry : prefs.entrySet()) {
                editor.putString(entry.getKey(), entry.getValue());
            }
            editor.apply();
        } catch (GeneralSecurityException | IOException e) {
            e.printStackTrace();
        }
    }

    public static HashMap<String, String> loadEncryptedPrefs(String file, Context context) {
        HashMap<String, String> prefs = new HashMap<>();
        try {
            SharedPreferences myPrefs = getEncryptedPrefs(file, context);
            Map<String, ?> allEntries = myPrefs.getAll();
            for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
                Object value = entry.getValue();
                if (value instanceof String) {
                    prefs.put(entry.getKey(), (String) value);
                } else {
                    prefs.put(entry.getKey(), String.valueOf(value));
                }
            }
        } catch (GeneralSecurityException | IOException e) {
            e.printStackTrace();
        }
        return prefs;
    }


}
