package io.github.server;


import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryonet.Server;

import java.io.IOException;

import io.github.server.config.ServerSpecConfig;
import io.github.server.data.network.ServerNetwork;
import io.github.server.network.kryolistener.AdminListener;
import io.github.server.network.kryolistener.AuthListener;
import io.github.server.network.kryolistener.DeckListener;
import io.github.server.network.kryolistener.MatchMakingListener;
import io.github.shared.local.data.network.KryoRegistry;

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

        this.kryoMotherServer.start();

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







    }


    public static void main(String[] args) {

        new ServerLauncher();

    }

}
