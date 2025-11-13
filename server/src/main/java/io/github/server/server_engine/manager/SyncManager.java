package io.github.server.server_engine.manager;

import com.esotericsoftware.kryonet.Connection;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.github.server.data.ServerGame;
import io.github.server.data.network.ServerNetwork;
import io.github.server.server_engine.factory.KryoMessagePackager;
import io.github.server.server_engine.factory.RequestFactory;
import io.github.server.server_engine.utils.PlayerChecker;
import io.github.shared.local.data.EnumsTypes.SyncType;
import io.github.shared.local.data.NetGame;
import io.github.shared.local.data.network.ClientNetwork;
import io.github.shared.local.data.network.KryoMessage;
import io.github.shared.local.data.requests.SynchronizeRequest;

public final class SyncManager {

    private ExecutorService executor = Executors.newFixedThreadPool(2);
    private static final SyncManager INSTANCE = new SyncManager();

    public static SyncManager getInstance() {
        return INSTANCE;
    }

    public void handleSyncRequest(Connection connection, SynchronizeRequest request){

        ClientNetwork client = ServerNetwork.getInstance().getClientByConnection(connection);
        if(client==null) return;

        SyncType type = request.getType();

        switch (type) {
            case FULL_RESYNC:
                System.out.println("Client is asking for a full sync : " + client.getUsername() + " " + client.getIp() );
                this.syncPlayer(client);
                break;

            case PARTIAL_RESYNC:
                break;

            default:
                break;
        }

    }

    public void syncPlayer(ClientNetwork client){
        ServerGame sgame = PlayerChecker.getGameOfClient(client);
        if(sgame==null) { System.out.println("Player asking for Sync not found in any game !"); return;}
        NetGame netGame = new NetGame(sgame);

        executor.submit( ()-> {
            // Crée la request avec le type correct
            SynchronizeRequest request = RequestFactory.createSynchronizeRequest(client, netGame);

            // Pack et envoie
            KryoMessage kryoMessage = KryoMessagePackager.packageSyncRequest(request);

                client.getConnection().sendTCP(kryoMessage);
        });
    }
}
