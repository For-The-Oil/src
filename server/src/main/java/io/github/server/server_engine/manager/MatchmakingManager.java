package io.github.server.server_engine.manager;

import static io.github.shared.data.enums_types.MatchModeType.CONFIRM;
import static io.github.shared.data.enums_types.MatchModeType.LEAVE;

import com.esotericsoftware.kryonet.Connection;
import java.util.ArrayList;
import java.util.HashMap;

import io.github.server.data.ServerGame;
import io.github.server.data.network.ServerNetwork;
import io.github.server.game_engine.GameLauncher;
import io.github.server.server_engine.factory.KryoMessagePackager;
import io.github.server.server_engine.factory.RequestFactory;
import io.github.server.server_engine.utils.GameMaker;
import io.github.shared.data.enums_types.GameModeType;
import io.github.shared.data.enums_types.MatchModeType;
import io.github.shared.data.network.ClientNetwork;
import io.github.shared.data.network.KryoMessage;
import io.github.shared.data.network.Player;
import io.github.shared.data.requests.MatchMakingRequest;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Singleton pour gérer le matchmaking des clients.
 */
public final class MatchmakingManager {

    private static final MatchmakingManager INSTANCE = new MatchmakingManager();

    public static MatchmakingManager getInstance() {
        return INSTANCE;
    }

    private MatchmakingManager() { }

    /**
     * Ajoute un client à une queue de matchmaking.
     * Si le client est déjà dans la queue, il n'est pas ajouté une deuxième fois.
     *
     * @param connection La connexion du client
     * @param newMode Le mode de jeu pour la queue
     */
    public void addToQueue(Connection connection, GameModeType newMode) {
        if (connection == null) return;

        ServerNetwork serverNetwork = ServerNetwork.getInstance();
        ClientNetwork client = serverNetwork.getClientByConnection(connection);
        if (client == null) return;

        HashMap<GameModeType, ArrayList<ClientNetwork>> queues = serverNetwork.getMatchmakingMap();

        synchronized (queues) {
            // Vérifie si le client est déjà dans une autre queue
            for (GameModeType mode : queues.keySet()) {
                ArrayList<ClientNetwork> queue = queues.get(mode);
                if (queue != null && queue.contains(client) && mode != newMode) {
                    queue.remove(client);
                    System.out.println("Client retiré de l'ancienne queue : " + client.getUsername() + " Mode: " + mode);
                    sendMatchmakingNotification(client, "Vous avez été retiré de la file " + mode, mode, LEAVE);
                }
            }

            // Ajout dans la nouvelle queue si pas déjà présent
            ArrayList<ClientNetwork> newQueue = queues.computeIfAbsent(newMode, k -> new ArrayList<>());
            if (!newQueue.contains(client)) {
                newQueue.add(client);
                System.out.println("Client ajouté à la queue : " + client.getUsername() + " Mode: " + newMode);
                sendMatchmakingNotification(client, "Vous avez été ajouté à la file " + newMode, newMode, CONFIRM);

                tryStartGame(newMode);
            }
        }
    }


    /**
     * Retire un client d'une queue de matchmaking si présent.
     *
     * @param connection La connexion du client
     * @param mode Le mode de jeu de la queue
     */
    public void removeFromQueue(Connection connection, GameModeType mode) {
        if (connection == null) return;

        ServerNetwork serverNetwork = ServerNetwork.getInstance();
        ClientNetwork client = serverNetwork.getClientByConnection(connection);
        if (client == null) return;

        HashMap<GameModeType, ArrayList<ClientNetwork>> queues = serverNetwork.getMatchmakingMap();

        synchronized (queues) {
            ArrayList<ClientNetwork> queue = queues.get(mode);
            if (queue != null && queue.remove(client)) {
                System.out.println("Client retiré de la queue : " + client.getUsername() + " Mode: " + mode);
                sendMatchmakingNotification(client, "Vous avez été retiré de la file " + mode, mode, LEAVE);
            }
        }
    }

    /**
     * Retire un client de toutes les queues de matchmaking (par exemple à la déconnexion).
     *
     * @param connection La connexion du client
     */
    public void removeFromAllQueues(Connection connection) {
        if (connection == null) return;

        ServerNetwork serverNetwork = ServerNetwork.getInstance();
        ClientNetwork client = serverNetwork.getClientByConnection(connection);
        if (client == null) return;

        HashMap<GameModeType, ArrayList<ClientNetwork>> queues = serverNetwork.getMatchmakingMap();

        synchronized (queues) {
            for (GameModeType mode : queues.keySet()) {
                ArrayList<ClientNetwork> queue = queues.get(mode);
                if (queue != null && queue.remove(client)) {
                    System.out.println("Client retiré de la queue : " + client.getUsername() + " Mode: " + mode);
                }
            }
        }
    }

    /**
     * Envoie une notification KryoMessage au client concernant le matchmaking.
     *
     * @param client  Le client à notifier
     * @param message Le message texte
     */
    private void sendMatchmakingNotification(ClientNetwork client, String message, GameModeType mode, MatchModeType command) {
        if (client == null || client.getConnection() == null) return;

        // Crée la request avec le type correct
        MatchMakingRequest request = RequestFactory.createMatchMakingRequest(mode, message, command);

        // Pack et envoie
        KryoMessage kryoMessage = KryoMessagePackager.packMatchMakingRequest(request);
        client.getConnection().sendTCP(kryoMessage);
    }

    private void sendMatchmakingNotification(ClientNetwork client, String message, GameModeType mode, MatchModeType command, HashMap<String, String> keys) {
        if (client == null || client.getConnection() == null) return;

        // Crée la request avec le type correct
        MatchMakingRequest request = RequestFactory.createMatchMakingRequest(mode, message, command, keys);

        // Pack et envoie
        KryoMessage kryoMessage = KryoMessagePackager.packMatchMakingRequest(request);
        client.getConnection().sendTCP(kryoMessage);
    }



    private void tryStartGame(GameModeType mode) {
        ArrayList<ClientNetwork> queue = ServerNetwork.getInstance().getMatchmakingMap().get(mode);
        if (queue == null) return;

        int minPlayers = mode.getMIN_PLAYER();
        int maxPlayers = mode.getMAX_PLAYER();

        while (queue.size() >= minPlayers) {
            ArrayList<ClientNetwork> playersForGame = new ArrayList<>();

            // On prend minPlayers d'abord
            for (int i = 0; i < minPlayers; i++) {
                playersForGame.add(queue.remove(0));
            }

            // On complète jusqu'à maxPlayers si possible
            int remainingSlots = Math.min(queue.size(), maxPlayers - minPlayers);
            for (int i = 0; i < remainingSlots; i++) {
                playersForGame.add(queue.remove(0));
            }

            ServerGame serverGame = createGame(playersForGame, mode);
            startGame(serverGame);
        }
    }

    public ServerGame createGame(ArrayList<ClientNetwork> playersForGame, GameModeType mode) {
        System.out.println("Trying to create a game in "+mode+" and the is "+playersForGame.size()+" players.");
        return GameMaker.createGame(playersForGame, mode);
    }


    public void startGame(ServerGame serverGame) {
        if (serverGame == null) return;

        // Crée un thread pour la game
        GameLauncher gameLauncher = new GameLauncher(serverGame);
        ServerNetwork.getInstance().getGameMapByUUID().put(serverGame.getGAME_UUID(), gameLauncher);
        gameLauncher.start();

        System.out.println("Game thread started: " + serverGame.getGAME_UUID() + " on thread " + gameLauncher.getName());

        // -----------------------------
        // Planifier la notification avec un délai (via ScheduledExecutor)
        // -----------------------------
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

        scheduler.schedule(() -> {
            HashMap<String, String> myMap = new HashMap<>();

            for (Player p : serverGame.getPlayersList()) {
                ClientNetwork activeClient = ServerNetwork.getInstance().getActiveClientByUUID(p.getUuid());
                if (activeClient == null) {
                    System.out.println("?? Player " + p.getUsername() + " is no longer connected ? skipping.");
                    continue;
                }

                sendMatchmakingNotification(activeClient, "Game started !",
                    serverGame.getGameMode(), MatchModeType.FOUND, myMap);
            }

            System.out.println("? All connected players notified for game: " + serverGame.getGAME_UUID());
            scheduler.shutdown();
        }, 2000, TimeUnit.MILLISECONDS);

    }

}
