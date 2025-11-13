package io.github.android.manager;

import android.util.Log;

import java.util.HashMap;
import io.github.core.data.gameobject.ClientGame;
import io.github.shared.local.data.EnumsTypes.KryoMessageType;
import io.github.shared.local.data.EnumsTypes.SyncType;
import io.github.shared.local.data.IGame;
import io.github.shared.local.data.network.KryoMessage;
import io.github.shared.local.data.requests.SynchronizeRequest;

public class GameManager {

    private static GameManager INSTANCE;
    private ClientGame game;
    private final Object gameLock = new Object();

    private GameManager(){}

    public static GameManager getInstance() {
        if(INSTANCE == null) INSTANCE = new GameManager();
        return INSTANCE;
    }

    public void askForFullGameSync() {
        Log.d("For The Oil", "Asking for full game sync");
        SynchronizeRequest request = new SynchronizeRequest(SyncType.FULL_RESYNC, new HashMap<>());
        KryoMessage kryo = new KryoMessage(KryoMessageType.SYNC, SessionManager.getInstance().getToken(), request);
        ClientManager.getInstance().getKryoManager().send(kryo);
    }

    public void syncFullGame(IGame game) {
        synchronized (gameLock) {
            this.game = new ClientGame(game.getWorld(), game.getGameMode(), game.getMapName(), game.getMap());
            this.game.setEntities(game.getEntities());
            this.game.setCurrentEvent(game.getCurrentEvent());
            this.game.setPlayersList(game.getPlayersList());
            this.game.setPlayerTeam(game.getPlayerTeam());
            this.game.setTime_left(game.getTime_left());
            this.game.setRunning(game.isRunning());
        }
    }




    public void setGame(ClientGame game) {
        synchronized (gameLock) {
            this.game = game;
        }
    }

    public ClientGame getGame() {
        synchronized (gameLock) {
            return game;
        }
    }

}
