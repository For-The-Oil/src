package io.github.server.server_engine.kryolistener;

import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;

import io.github.server.server_engine.manager.SyncManager;
import io.github.shared.local.data.network.KryoMessage;
import io.github.shared.local.data.requests.SynchronizeRequest;

public class GameListener extends Listener {

    @Override
    public void connected(Connection connection) {
        System.out.println("[GameListener] Client connected to game channel: " + connection.getRemoteAddressTCP());
    }

    @Override
    public void disconnected(Connection connection) {
        System.out.println("[GameListener] Client disconnected from game channel: " + connection.getRemoteAddressTCP());
        // Tu pourrais ici gérer le fait qu’un joueur quitte sa partie brutalement
    }

    @Override
    public void received(Connection connection, Object object) {
        if (!(object instanceof KryoMessage)) return;

        KryoMessage kryo = (KryoMessage) object;

        if(! (kryo.getObj() instanceof SynchronizeRequest)) return;

        System.out.println("SynchronizeRequest Received !");

        SynchronizeRequest request = (SynchronizeRequest) kryo.getObj();

        SyncManager.getInstance().handleSyncRequest(connection, request);

    }

}
