package io.github.server.server_engine.kryolistener;

import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;

import io.github.server.data.ServerGame;
import io.github.server.game_engine.GameLauncher;
import io.github.server.server_engine.manager.SyncManager;
import io.github.server.server_engine.utils.PlayerChecker;
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
            handleSynchroRequest(connection, object, kryo);


        if(kryo.getObj() instanceof AttackGroupRequest){

        }

        if(kryo.getObj() instanceof BuildRequest){

        }

        if (kryo.getObj() instanceof CastRequest){

        }

        if(kryo.getObj() instanceof DestroyRequest){

        }

        if(kryo.getObj() instanceof MoveGroupRequest){

        }

        if(kryo.getObj() instanceof SummonRequest){

        }




    }


    private void handleSynchroRequest(Connection connection, Object object, KryoMessage kryo){
        System.out.println("SynchronizeRequest Received !");

        SynchronizeRequest request = (SynchronizeRequest) kryo.getObj();

        SyncManager.getInstance().handleSyncRequest(connection, request);
    }

    private void handleGameRequest(Request request){
        // TODO: ADD GameLogic
        //GameLauncher gameLauncher = PlayerChecker.getGameOfClient(client);
        System.out.println("GameRequest Received !");
        //gameLauncher.addQueueRequest(request);
    }








}
