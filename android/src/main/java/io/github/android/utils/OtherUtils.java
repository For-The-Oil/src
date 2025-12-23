package io.github.android.utils;

import static io.github.android.config.ClientDefaultConfig.SERVER_PREFS;

import android.content.Context;

import java.util.HashMap;
import java.util.Map;

import io.github.core.config.ServerDefaultConfig;
import io.github.android.listeners.ClientListener;
import io.github.android.manager.ClientManager;
import io.github.core.client_engine.manager.SessionManager;
import io.github.shared.data.enums_types.ResourcesType;

public final class OtherUtils {

    public static int getIntOrDefault(Map<String, String> map, String key, int defaultValue) {
        String value = map.get(key);
        if (value == null || value.isEmpty()) return defaultValue;
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    public static void initClientConfig(Context context) {
        ClientManager myClientManager = ClientManager.getInstance();

        HashMap<String, String> saved = PrefsUtils.loadPrefs(SERVER_PREFS, context);

        String ip = saved.getOrDefault("server_ip", ServerDefaultConfig.SERVER_HOST);
        int port = getIntOrDefault(saved, "server_port", ServerDefaultConfig.SERVER_PORT);

        myClientManager.setIP(ip);
        myClientManager.setPort(port);

        myClientManager.getKryoManager().addListener(ClientListener.getInstance());

        SessionManager.getInstance().clearSession();
    }


    public static boolean checkAutoConnect(Context context){
        HashMap<String, String> saved = PrefsUtils.loadPrefs(SERVER_PREFS, context);
        String auto = saved.getOrDefault("auto_login", "false");
        return "true".equalsIgnoreCase(auto);
    }


    /**
     * Fonction servant à calculer si un batiment peut être construit par un joueur.
     * @param playerResources
     * @param cost
     * @return
     */
    public static boolean canAfford(Map<ResourcesType, Integer> playerResources, Map<ResourcesType, Integer> cost) {
        if (cost == null || cost.isEmpty()) {
            return true;
        }

        if (playerResources == null) {
            return false;
        }

        for (Map.Entry<ResourcesType, Integer> entry : cost.entrySet()) {
            int required = entry.getValue();
            int owned = playerResources.getOrDefault(entry.getKey(), 0);

            if (owned < required) {
                return false;
            }
        }

        return true;
    }


}
