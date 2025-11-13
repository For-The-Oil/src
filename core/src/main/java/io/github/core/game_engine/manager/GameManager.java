package io.github.core.game_engine.manager;


import io.github.core.game_engine.ClientGame;
import io.github.shared.local.data.NetGame;
import io.github.shared.local.data.snapshot.EntitySnapshot;
import io.github.shared.local.shared_engine.Utility;
import io.github.shared.local.shared_engine.factory.EntityFactory;
import io.github.shared.local.shared_engine.manager.EcsManager;

public class GameManager {
    public static void fullGameResync(NetGame netGame,ClientGame clientgame) {
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
