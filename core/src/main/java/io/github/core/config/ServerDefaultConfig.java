package io.github.core.config;

public final class ServerDefaultConfig {

    // Adresse et port du serveur
    public static final String SERVER_HOST = "127.0.0.1";
    public static final int SERVER_PORT = 54555;


    // Timeout de connexion (ms)
    public static final int CONNECT_TIMEOUT = 5000;


    // Taille des buffers KryoNet
    public static final int WRITE_BUFFER = 64 * 1024;
    public static final int OBJECT_BUFFER = 32 * 1024;


}
