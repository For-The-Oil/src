package io.github.server.server_engine.kryolistener;

import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;

import io.github.server.data.ServerGame;
import io.github.server.game_engine.GameLauncher;
import io.github.server.server_engine.manager.SyncManager;
import io.github.server.server_engine.utils.PlayerChecker;
import io.github.shared.data.network.ClientNetwork;
import io.github.shared.data.network.KryoMessage;
import io.github.shared.data.requests.Request;
import io.github.shared.data.requests.SynchronizeRequest;
import io.github.shared.data.requests.game.AttackGroupRequest;
import io.github.shared.data.requests.game.BuildRequest;
import io.github.shared.data.requests.game.CastRequest;
import io.github.shared.data.requests.game.DestroyRequest;
import io.github.shared.data.requests.game.MoveGroupRequest;
import io.github.shared.data.requests.game.SummonRequest;

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

        if(kryo.getObj() instanceof SynchronizeRequest)
            handleSynchroRequest(connection, (SynchronizeRequest) kryo.getObj());

        if(kryo.getObj() instanceof AttackGroupRequest ||
            kryo.getObj() instanceof BuildRequest ||
            kryo.getObj() instanceof CastRequest ||
            kryo.getObj() instanceof DestroyRequest ||
            kryo.getObj() instanceof MoveGroupRequest ||
            kryo.getObj() instanceof SummonRequest){
            handleGameRequest((Request) kryo.getObj());
        }

    }


    private void handleSynchroRequest(Connection connection, SynchronizeRequest synchronizeRequest){
        System.out.println("SynchronizeRequest Received !");
        SyncManager.getInstance().handleSyncRequest(connection, synchronizeRequest);
    }

    private void handleGameRequest(Request request){
        GameLauncher gameLauncher = PlayerChecker.getGameLauncherOfClient(request.getPlayer());
        System.out.println("GameRequest Received !");
        gameLauncher.addQueueRequest(request);
    }








}
