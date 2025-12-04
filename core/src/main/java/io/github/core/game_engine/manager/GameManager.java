package io.github.core.game_engine.manager;


import io.github.core.data.ClientGame;
import io.github.shared.data.NetGame;
import io.github.shared.data.snapshot.EntitySnapshot;
import io.github.shared.shared_engine.Utility;
import io.github.shared.shared_engine.factory.EntityFactory;
import io.github.shared.shared_engine.manager.EcsManager;

public class GameManager {
    public static void fullGameResync(NetGame netGame) {
        if(ClientGame.isInstanceNull() || !netGame.getGameMode().equals(ClientGame.getInstance().getGameMode()) || !netGame.getMapName().equals(ClientGame.getInstance().getMapName())){
            ClientGame.setInstance(netGame.getGameMode(), netGame.getMapName(), netGame.getMap(), netGame.getGAME_UUID(),netGame.getTime_left());
        }
        EcsManager.filterEntitiesByNetId(ClientGame.getInstance().getWorld(),Utility.extractNetIds(netGame.getEntities()));
        for(EntitySnapshot es : netGame.getEntities())EntityFactory.applySnapshotToEntity(ClientGame.getInstance().getWorld(),es);

        ClientGame.getInstance().setMap(netGame.getMap());
        ClientGame.getInstance().setCurrentEvent(netGame.getCurrentEvent());
        ClientGame.getInstance().setPlayersList(netGame.getPlayersList());
        ClientGame.getInstance().setPlayerTeam(netGame.getPlayerTeam());
        ClientGame.getInstance().setTime_left(netGame.getTime_left());
        ClientGame.getInstance().setRunning(netGame.isRunning());
    }


}
