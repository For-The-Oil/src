package io.github.android.config;

public final class ClientDefaultConfig {

    public static String SERVER_PREFS = "server_prefs"; // File where we store data like the IP/PORT of the server
    public static String CLIENT_SECURE = "client_secure"; // File where we store the login information of the clients (it's a crypted file)
    public static int INIT_WAITING_TIME = 400; // The wait time for each transitions

}
