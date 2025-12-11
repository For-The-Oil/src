package io.github.server.server_engine.manager;

import com.esotericsoftware.kryonet.Connection;

import java.awt.Component;
import java.util.ArrayList;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import io.github.server.data.ServerGame;
import io.github.server.data.network.ServerNetwork;
import io.github.server.game_engine.manager.SnapshotTracker;
import io.github.server.server_engine.factory.KryoMessagePackager;
import io.github.server.server_engine.factory.RequestFactory;
import io.github.server.server_engine.utils.PlayerChecker;
import io.github.shared.data.enums_types.SyncType;
import io.github.shared.data.NetGame;
import io.github.shared.data.instructions.Instruction;
import io.github.shared.data.instructions.UpdateEntityInstruction;
import io.github.shared.data.network.ClientNetwork;
import io.github.shared.data.network.KryoMessage;
import io.github.shared.data.network.Player;
import io.github.shared.data.requests.SynchronizeRequest;
import io.github.shared.data.snapshot.ComponentSnapshot;
import io.github.shared.data.snapshot.EntitySnapshot;

public final class SyncManager {

    private static final SyncManager INSTANCE = new SyncManager();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private static final ExecutorService executor = Executors.newCachedThreadPool();

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
                this.syncPlayer(client,PlayerChecker.getGameOfClient(client));
                break;

            case INSTRUCTION_SYNC:

                break;

            default:
                break;
        }

    }

    public void syncPlayer(ClientNetwork client,ServerGame sgame){
        if (sgame == null) {
            System.out.println("Player asking for Sync not found in any game !");
            return;
        }

        System.out.println("Player found game = " + sgame);

        NetGame netGame = new NetGame(sgame);

        // Envoie dans 0.5 secondes
        scheduler.schedule(() -> {
            try {
                System.out.println("Scheduled task started !");

                SynchronizeRequest request = RequestFactory.createSynchronizeRequest(client, netGame);
                KryoMessage kryoMessage = KryoMessagePackager.packageSyncRequest(request);
                client.getConnection().sendTCP(kryoMessage);

                System.out.println("Sync request sent.");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, 500, TimeUnit.MILLISECONDS);

    }


    public static void sendInstructions(Queue<Instruction> networkQueue, ArrayList<Player> playersList){
        if(networkQueue==null || playersList==null) return;

        for (Player player: playersList) {
            if(player.getConnection().isConnected()) {
                executor.execute(() -> {
                    try {
                        SynchronizeRequest request = RequestFactory.createSynchronizeInstructions(player, networkQueue);
                        KryoMessage kryoMessage = KryoMessagePackager.packageSyncRequest(request);
                        player.getConnection().sendTCP(kryoMessage);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });

            }

        }

    }


}
