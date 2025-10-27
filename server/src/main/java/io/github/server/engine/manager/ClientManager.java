package io.github.server.engine.manager;

import com.esotericsoftware.kryonet.Connection;

import java.util.ArrayList;

import io.github.server.data.network.ServerNetwork;
import io.github.shared.local.data.network.ClientNetwork;
import io.github.shared.local.data.requests.AuthRequest;

public final class ClientManager {

    private static final ClientManager INSTANCE = new ClientManager();

    public static ClientManager getInstance() {
        return INSTANCE;
    }
    private ClientManager(){

    }
    public void loginClient(Connection connection, AuthRequest object){
        System.out.println("Client is asking for a login ...");

        if (!checkCredentials()) {
            System.out.println("Invalid credentials !");
            return;
        }

        ClientManager.getInstance().addClient(connection, object);

    }

    public void loginByTokenClient(Connection connection, AuthRequest object){
        System.out.println("Client is asking for a login by token ...");
    }

    public void registerClient(Connection connection, AuthRequest object){
        System.out.println("Client is asking to register ...");
    }



    // Méthode fictive pour vérification des identifiants
    private boolean checkCredentials() {
        return true;
    }

    public boolean canRegisterClient(){
        return true;
    }

    public void registerClient(){

    }

    public void addClient(Connection connection, AuthRequest obj) {
        ServerNetwork intern = ServerNetwork.getInstance();
        ArrayList<ClientNetwork> clientList = intern.getAuthClientNetworkList();

    }

    public void removeClient(){

    }










}
