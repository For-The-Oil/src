package io.github.server.server_engine.utils;

import java.util.concurrent.TimeUnit;

import io.github.server.data.Game;
import io.github.server.data.network.ServerNetwork;
import io.github.server.game_engine.GameLauncher;
import io.github.shared.local.data.network.Player;

/**
 * Utility class providing static CLI helper methods
 * for the server administration console.
 */
public final class CliHelpers {

    // Private constructor to prevent instantiation
    private CliHelpers() {}

    public static void printHelp() {
        System.out.println(
            "Available commands:\n" +
                "- help            : Show this help message\n" +
                "- stats           : Display server statistics\n" +
                "- clients         : List connected authenticated clients\n" +
                "- games           : List active games\n" +
                "- matchmaking     : Show matchmaking queues\n" +
                "- uptime          : Show how long the server has been running\n" +
                "- stop            : Shutdown the server\n" +
                "- stopgame <UUID> : Stop a running game by its UUID"
        );
    }

    public static void printStats() {
        ServerNetwork net = ServerNetwork.getInstance();
        System.out.println("=== Server Stats ===");
        System.out.println("Clients connected  : " + net.getAuthClientNetworkList().size());
        System.out.println("Games active       : " + net.getGameMapByUUID().size());
        int totalMatchmakingClients = net.getMatchmakingMap().values().stream()
            .mapToInt(list -> list.size())
            .sum();
        System.out.println("Matchmaking clients: " + totalMatchmakingClients);
    }

    public static void printClients() {
        ServerNetwork net = ServerNetwork.getInstance();
        if (net.getAuthClientNetworkList().isEmpty()) {
            System.out.println("No authenticated clients.");
            return;
        }
        System.out.println("=== Connected Clients ===");
        net.getAuthClientNetworkList().forEach(client ->
            System.out.println(" - " + client)
        );
    }

    public static void printGames() {
        ServerNetwork net = ServerNetwork.getInstance();

        if (net.getGameMapByUUID().isEmpty()) {
            System.out.println("No active games.");
            return;
        }

        System.out.println("=== Active Games ===");
        net.getGameMapByUUID().forEach((uuid, launcher) -> {
            Game game = launcher.getGame(); // récupérer la game depuis le launcher
            String indentGame = "  ";   // indent pour le jeu
            String indentPlayer = "    "; // indent pour les joueurs

            // Infos principales du jeu
            System.out.println(indentGame + "GameLauncher Thread: " + launcher.getName());
            System.out.println(indentGame + "Game UUID: " + game.getGAME_UUID());
            System.out.println(indentGame + "Map: " + game.getMapType());
            System.out.println(indentGame + "Current Event: " + game.getCurrentEvent());
            System.out.println(indentGame + "Time Left: " + game.getTime_left() + "s");

            // Liste des joueurs
            System.out.println(indentGame + "Players in game:");
            for (Player p : game.getPlayersList()) {
                System.out.println(indentPlayer + "- " + p.getUsername() + " (UUID: " + p.getUuid() + ")");
            }

            System.out.println(indentGame + "-----------------------------------");
        });
    }

    public static void printMatchmaking() {
        ServerNetwork net = ServerNetwork.getInstance();
        System.out.println("=== Matchmaking Queues ===");

        if (net.getMatchmakingMap().isEmpty()) {
            System.out.println("No players waiting in matchmaking.");
            return;
        }

        net.getMatchmakingMap().forEach((mode, clients) -> {
            System.out.println("Mode: " + mode);
            if (clients.isEmpty()) {
                System.out.println("   -> No players in queue");
            } else {
                System.out.println("   -> Players (" + clients.size() + "):");
                clients.forEach(client ->
                    System.out.println("      - " + client.getUsername() +
                        " (" + client.getConnection().getRemoteAddressTCP() + ")")
                );
            }
        });
    }

    public static void printUptime() {
        ServerNetwork net = ServerNetwork.getInstance();
        long uptimeMs = System.currentTimeMillis() - net.getStart_timestamp();
        long hours = TimeUnit.MILLISECONDS.toHours(uptimeMs);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(uptimeMs) % 60;
        long seconds = TimeUnit.MILLISECONDS.toSeconds(uptimeMs) % 60;

        System.out.printf("Server Uptime: %02dh %02dm %02ds%n", hours, minutes, seconds);
    }

    /**
     * Stop a running game by its UUID.
     * @param gameUUID UUID of the game to stop
     */
    public static void stopGameByUUID(java.util.UUID gameUUID) {
        ServerNetwork net = ServerNetwork.getInstance();
        GameLauncher launcher = net.getGameMapByUUID().get(gameUUID);

        if (launcher == null) {
            System.out.println("No active game found with UUID: " + gameUUID);
            return;
        }

        System.out.println("Stopping game: " + gameUUID + " (Thread: " + launcher.getName() + ")");
        launcher.stopGame();

        // Supprimer de la map pour ne plus la considérer active
        net.getGameMapByUUID().remove(gameUUID);

        System.out.println("Game stopped successfully.");
    }

}
