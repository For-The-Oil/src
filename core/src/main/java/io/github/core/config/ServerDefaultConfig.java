package io.github.core.config;

public final class ServerDefaultConfig {

    // Adresse et port du serveur
    public static final String SERVER_HOST = "10.0.2.2"; //The default IP
    public static final int SERVER_PORT = 54555; // The default PORT

    // Timeout de connexion (ms)
    public static final int CONNECT_TIMEOUT = 5000;


    // Taille des buffers KryoNet
    public static final int WRITE_BUFFER = 2 * 1024 * 1024; // 2 Mo
    public static final int OBJECT_BUFFER = 2 * 1024 * 1024; // 2 Mo

}
