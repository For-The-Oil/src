package io.github.android.utils;

import java.util.Map;

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

}
