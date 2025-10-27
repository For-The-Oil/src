package io.github.server.manager;

import com.esotericsoftware.kryonet.Connection;

import java.util.ArrayList;
import java.util.HashMap;

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
