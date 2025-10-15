package io.github.server;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;

import java.io.IOException;
import java.util.Scanner;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Launches a KryoNet TCP server that accepts one connection and exchanges Strings.
 */
public class ServerLauncher {

    // Port TCP par défaut (KryoNet utilise aussi UDP si besoin, ici on bind uniquement TCP)
    private static final int DEFAULT_TCP_PORT = 54555;

    public static void main(String[] args) {
        int tcpPort = DEFAULT_TCP_PORT;
        if (args.length >= 1) {
            try {
                tcpPort = Integer.parseInt(args[0]);
            } catch (NumberFormatException ignored) {
                System.out.println("Argument non valide pour le port, utilisation du port par défaut: " + DEFAULT_TCP_PORT);
            }
        }

        Server server = new Server();
        // Permet d'obtenir des logs KryoNet si besoin :
        // server.addListener(new Listener() { ... } )

        // Register classes we will send/receive. For simple Strings this is not strictly required,
        // but registering is good practice and needed for custom classes.
        Kryo kryo = server.getKryo();
        kryo.register(String.class);

        // AtomicReference pour stocker la première connexion
        AtomicReference<Connection> clientConnection = new AtomicReference<>(null);
        // Latch pour signaler qu'une connexion a été acceptée
        CountDownLatch connectionLatch = new CountDownLatch(1);

        server.addListener(new Listener() {
            @Override
            public void connected(Connection connection) {
                // Si on n'a pas encore de client, on prend la première connexion entrante.
                if (clientConnection.compareAndSet(null, connection)) {
                    System.out.println("Client connecté : " + connection.getRemoteAddressTCP());
                    connectionLatch.countDown();
                    // Envoi un message de bienvenue
                    connection.sendTCP("SERVER: Bienvenue ! Tapez 'exit' pour quitter.");
                } else {
                    // Si un second client tente de se connecter, on peut le déconnecter immédiatement
                    System.out.println("Une deuxième connexion a été tentée et sera refusée : " + connection.getRemoteAddressTCP());
                    connection.close(); // ferme la nouvelle connexion
                }
            }

            @Override
            public void received(Connection connection, Object object) {
                // N'accepter que les strings et uniquement venant du client sélectionné
                Connection primary = clientConnection.get();
                if (connection != primary) return;

                if (object instanceof String) {
                    String msg = (String) object;
                    System.out.println("CLIENT: " + msg);
                    if ("exit".equalsIgnoreCase(msg.trim())) {
                        System.out.println("Client demandé de terminer la connexion (exit).");
                        connection.close();
                        // on arrêtera le serveur dans le thread principal après la fermeture
                    } else {
                        // Exemple : renvoyer un echo (optionnel)
                        connection.sendTCP("SERVER(ECHO): " + msg);
                    }
                } else {
                    System.out.println("Reçu un objet non attendu: " + object.getClass().getName());
                }
            }

            @Override
            public void disconnected(Connection connection) {
                // Si le client principal se déconnecte, on arrête le serveur proprement.
                Connection primary = clientConnection.get();
                if (connection == primary) {
                    System.out.println("Client déconnecté : " + connection.getRemoteAddressTCP());
                } else {
                    System.out.println("Une connexion non-principale s'est déconnectée.");
                }
            }
        });

        try {
            server.start();
            server.bind(tcpPort);
            System.out.println("Serveur démarré, en attente d'une connexion sur le port TCP " + tcpPort + "...");

            // Attendre la première connexion (timeout optionnel)
            boolean connected = connectionLatch.await(120, TimeUnit.SECONDS);
            if (!connected) {
                System.out.println("Aucune connexion reçue pendant 120 secondes. Arrêt du serveur.");
                server.stop();
                return;
            }

            // Maintenant qu'un client est connecté, on lance un thread pour lire la console et envoyer des strings.
            Thread consoleThread = new Thread(() -> {
                Scanner scanner = new Scanner(System.in);
                System.out.println("Tapez des messages pour les envoyer au client (tapez 'exit' pour arrêter) :");
                while (true) {
                    if (!scanner.hasNextLine()) break;
                    String line = scanner.nextLine();
                    Connection conn = clientConnection.get();
                    if (conn == null || !conn.isConnected()) {
                        System.out.println("Client non connecté. Fermeture du thread console.");
                        break;
                    }
                    conn.sendTCP(line);
                    if ("exit".equalsIgnoreCase(line.trim())) {
                        System.out.println("Commande 'exit' envoyée. Arrêt en cours...");
                        break;
                    }
                }
                scanner.close();
            }, "Console-Sender");
            consoleThread.setDaemon(true);
            consoleThread.start();

            // Boucle d'attente principale : on surveille la connexion et on s'arrête si elle se ferme.
            while (true) {
                Connection conn = clientConnection.get();
                if (conn == null) {
                    // improbable ici, mais sécurité : attendre encore un peu
                    Thread.sleep(200);
                    continue;
                }
                if (!conn.isConnected()) {
                    System.out.println("Connexion principale fermée. Arrêt du serveur.");
                    break;
                }
                // On peut aussi ajouter une condition d'arrêt, timeout, etc.
                Thread.sleep(500);
            }

        } catch (IOException e) {
            System.err.println("Erreur lors du démarrage/bind du serveur: " + e.getMessage());
            e.printStackTrace();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            try {
                System.out.println("Arrêt du serveur...");
                server.stop();
            } catch (Exception e) {
                // ignore
            }
        }
    }
}
