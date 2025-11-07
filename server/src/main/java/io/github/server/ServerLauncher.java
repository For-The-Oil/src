package io.github.server;


import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryonet.Server;

import java.io.IOException;

import io.github.server.config.ServerSpecConfig;
import io.github.server.data.network.ServerNetwork;
import io.github.server.server_engine.kryolistener.AdminListener;
import io.github.server.server_engine.kryolistener.AuthListener;
import io.github.server.server_engine.kryolistener.DeckListener;
import io.github.server.server_engine.kryolistener.MatchMakingListener;
import io.github.server.server_engine.utils.CliHelpers;
import io.github.shared.local.data.network.KryoRegistry;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

/**
 * Main Class of the server
 *
 * Start a Kryo Server {@link Server} and add to it multiple Listeners such as :
 *  - AuthListener {@link AuthListener}
 *  - DeckListener {@link DeckListener}
 *  - MatchMakingListener {@link MatchMakingListener}
 *  - AdminListener {@link AdminListener}
 *
 */
public class ServerLauncher {

    private Server kryoMotherServer;
    private ServerNetwork internServer;


    public ServerLauncher(){
        init();
    }

    private void init() {
        // console print of usefull informations when the server start
        System.out.println("=============================================");
        System.out.println("         🚀 Server Starting ...              ");
        System.out.println("=============================================");
        System.out.println("Server Name           : " + ServerSpecConfig.SERVER_NAME);
        System.out.println("Server Version        : " + ServerSpecConfig.SERVER_VERSION);
        System.out.println("Client Version        : " + ServerSpecConfig.CLIENT_VERSION);
        System.out.println("Description           : " + ServerSpecConfig.SERVER_DESC);
        System.out.println("Base Port TCP         : " + ServerSpecConfig.BASE_PORT);
        System.out.println("Max Game Capacity     : " + ServerSpecConfig.SERVER_MAX_CAPACITY_GAME);
        System.out.println("Max Players Capacity  : " + ServerSpecConfig.SERVER_MAX_CAPACITY_PLAYERS);
        System.out.println("Write Buffer Size     : " + ServerSpecConfig.MAIN_SERVER_WRITE_BUFFER / 1024 + " Ko");
        System.out.println("Object Buffer Size    : " + ServerSpecConfig.MAIN_SERVER_OBJ_BUFFER / 1024 + " Ko");
        System.out.println("=============================================\n");

        // Instanciating the ServerNetwork
        this.internServer = ServerNetwork.getInstance();

        //Creation of the KryoMotherServer
        this.kryoMotherServer = new Server(ServerSpecConfig.MAIN_SERVER_WRITE_BUFFER, ServerSpecConfig.MAIN_SERVER_OBJ_BUFFER);

        Kryo kryo = kryoMotherServer.getKryo();

        try {
            //Adding the TCP port
            this.kryoMotherServer.bind(ServerSpecConfig.BASE_PORT);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // Register all the classes used by Kryo
        // Must be done before starting the server !
        KryoRegistry.registerAll(kryo);

        /*
         * Adding of the listeners
         */

        // Listener for the authentification
        kryoMotherServer.addListener(new AuthListener());

        // Listener for the Deck of the player
        kryoMotherServer.addListener(new DeckListener());

        // Listener for the MatchMaking
        kryoMotherServer.addListener(new MatchMakingListener());

        // Listener for the Admin Commands
        kryoMotherServer.addListener(new AdminListener());



        this.kryoMotherServer.start();

        if (System.console() == null) {
            System.out.println("Console input disabled because of a lack of console input.");
        } else {
            startConsole();
        }


    }
    private void startConsole() {
        new Thread(() -> {
            Scanner scanner = new Scanner(System.in);
            System.out.println("Console ready. Type 'help' for available commands.");

            while (true) {
                System.out.print("> ");
                String line = scanner.nextLine().trim();

                if (line.isEmpty()) continue;

                String[] parts = line.split("\\s+");
                String command = parts[0].toLowerCase();

                try {
                    switch (command) {
                        case "help":
                            CliHelpers.printHelp();
                            break;
                        case "stats":
                            CliHelpers.printStats();
                            break;
                        case "clients":
                            CliHelpers.printClients();
                            break;
                        case "games":
                            CliHelpers.printGames();
                            break;
                        case "matchmaking":
                            CliHelpers.printMatchmaking();
                            break;
                        case "uptime":
                            CliHelpers.printUptime();
                            break;
                        case "stop":
                            System.out.println("Stopping server...");
                            kryoMotherServer.stop();
                            System.exit(0);
                            break;
                        case "stopgame":
                            if (parts.length < 2) {
                                System.out.println("Usage: stopgame <UUID> !");
                                break;
                            }
                            try {
                                java.util.UUID gameUUID = java.util.UUID.fromString(parts[1]);
                                CliHelpers.stopGameByUUID(gameUUID);
                            } catch (IllegalArgumentException e) {
                                System.out.println("Invalid UUID format: " + parts[1]);
                            }
                            break;
                        default:
                            System.out.println("Unknown command. Type 'help' for a list.");
                    }
                } catch (Exception e) {
                    System.out.println("⚠ Error while executing command: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }, "ServerConsoleThread").start();
    }



    public static void main(String[] args) {

        new ServerLauncher();

    }

}
