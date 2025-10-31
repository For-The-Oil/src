package io.github.android.utils;

public final class FormatUtils {

    /**
     * Vérifie si la chaîne est une IPv4 valide.
     */
    public static boolean isValidIPAddress(String ip) {
        String regex = "^((25[0-5]|2[0-4]\\d|[01]?\\d\\d?)\\.){3}(25[0-5]|2[0-4]\\d|[01]?\\d\\d?)$";
        return ip.matches(regex);
    }

    /**
     * Vérifie si la chaîne est un hostname valide.
     */
    public static boolean isValidHostname(String hostname) {
        String regex = "^[a-zA-Z0-9.-]+$";
        return hostname.matches(regex);
    }

}
