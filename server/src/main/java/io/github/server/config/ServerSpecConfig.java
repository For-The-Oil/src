package io.github.server.config;

public final class ServerSpecConfig {

    public static int BASE_PORT = 54555;
    public static int MAIN_SERVER_WRITE_BUFFER = 64*1024; // 64Ko
    public static int MAIN_SERVER_OBJ_BUFFER = 32*1024;  // 32Ko

    public static String SERVER_VERSION = "0.1";
    public static String CLIENT_VERSION ="0.0";
    public static String SERVER_NAME = "DIVINE ENTERPRISE";
    public static String SERVER_DESC = "TEMPORARY DESC";
    public static int SERVER_MAX_CAPACITY_PLAYERS = 16;
    public static int SERVER_MAX_CAPACITY_GAME = 8;

}
