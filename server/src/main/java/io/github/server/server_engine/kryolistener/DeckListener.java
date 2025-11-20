package io.github.server.server_engine.kryolistener;

import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;

import io.github.server.server_engine.manager.DeckManager;
import io.github.shared.data.network.KryoMessage;
import io.github.shared.data.requests.DeckRequest;


public class DeckListener extends Listener {

    @Override
    public void connected(Connection connection) {

    }

    @Override
    public void disconnected(Connection connection) {

    }

    @Override
    public void received(Connection connection, Object object) {
        if (!(object instanceof KryoMessage)) return;

        KryoMessage kryo = (KryoMessage) object;

        if(! (kryo.getObj() instanceof DeckRequest)) return;

        System.out.println("DeckRequest Received !");

        DeckRequest request = (DeckRequest) kryo.getObj();

        DeckManager.getInstance().handleDeckRequest(connection, request);

    }


}
