package io.github.android.manager;

import android.util.Log;

import com.artemis.World;

import java.util.HashMap;
import io.github.core.data.gameobject.ClientGame;
import io.github.shared.local.data.EnumsTypes.KryoMessageType;
import io.github.shared.local.data.EnumsTypes.SyncType;
import io.github.shared.local.data.NetGame;
import io.github.shared.local.data.network.KryoMessage;
import io.github.shared.local.data.requests.SynchronizeRequest;
import io.github.shared.local.data.snapshot.EntitySnapshot;
import io.github.shared.local.shared_engine.Utility;
import io.github.shared.local.shared_engine.factory.EntityFactory;
import io.github.shared.local.shared_engine.manager.EcsManager;

public class GameManager {
    public static void askForFullGameSync() {
        Log.d("For The Oil", "Asking for full game sync");
        SynchronizeRequest request = new SynchronizeRequest(SyncType.FULL_RESYNC, new HashMap<>());
        KryoMessage kryo = new KryoMessage(KryoMessageType.SYNC, SessionManager.getInstance().getToken(), request);
        ClientManager.getInstance().getKryoManager().send(kryo);
    }

    public static void TheBigReplacement(NetGame netGame,ClientGame clientgame) {
            if(clientgame == null || !netGame.getGameMode().equals(clientgame.getGameMode()) || !netGame.getMapName().equals(clientgame.getMapName())){
                clientgame = new ClientGame(netGame.getGameMode(), netGame.getMapName(), netGame.getMap());
            }
            EcsManager.filterEntitiesByNetId(clientgame.getWorld(),Utility.extractNetIds(netGame.getEntities()));
            for(EntitySnapshot es : netGame.getEntities()){
            EntityFactory.applySnapshotToEntity(clientgame.getWorld(),es);
            }
            clientgame.setMap(netGame.getMap());
            clientgame.setCurrentEvent(netGame.getCurrentEvent());
            clientgame.setPlayersList(netGame.getPlayersList());
            clientgame.setPlayerTeam(netGame.getPlayerTeam());
            clientgame.setTime_left(netGame.getTime_left());
            clientgame.setRunning(netGame.isRunning());
    }
}
