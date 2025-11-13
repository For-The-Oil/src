package io.github.shared.local.data;

import com.artemis.Aspect;
import com.artemis.World;
import com.artemis.utils.IntBag;

import io.github.shared.local.data.EnumsTypes.EventType;
import io.github.shared.local.data.EnumsTypes.GameModeType;
import io.github.shared.local.data.EnumsTypes.MapName;
import io.github.shared.local.data.gameobject.Shape;
import io.github.shared.local.data.network.Player;
import io.github.shared.local.data.snapshot.EntitySnapshot;
import io.github.shared.local.shared_engine.factory.SnapshotFactory;

import java.util.*;

public class NetGame implements IGame {
    private final UUID gameUuid;
    private final boolean running;
    private final long timeLeft;
    private final GameModeType gameMode;
    private final MapName mapName;
    private final Shape map;
    private final EventType currentEvent;
    private final HashMap<String, ArrayList<Player>> playerTeam;
    private final ArrayList<Player> playersList;
    private final ArrayList<EntitySnapshot> entities;
    private final long lastTime;

    public NetGame(UUID gameUuid, boolean running, long timeLeft, GameModeType gameMode, MapName mapName, Shape map, EventType currentEvent, HashMap<String, ArrayList<Player>> playerTeam, ArrayList<Player> playersList, World world, long lastTime) {
        this.gameUuid = gameUuid;
        this.running = running;
        this.timeLeft = timeLeft;
        this.gameMode = gameMode;
        this.mapName = mapName;
        this.map = map;
        this.currentEvent = currentEvent;
        this.playerTeam = playerTeam;
        this.playersList = playersList;
        this.lastTime = lastTime;
        this.entities = new ArrayList<>();
        IntBag entitiesBag = world.getAspectSubscriptionManager().get(Aspect.all()).getEntities();
        for (int i = 0; i < entitiesBag.size(); i++) {
            int sourceEntityId = entitiesBag.get(i);
            this.entities.add(SnapshotFactory.fromEntity(world,world.getEntity(sourceEntityId)));
        }
    }

    public NetGame(IGame game){
        this(game.getGAME_UUID(),game.isRunning(),game.getTime_left(),game.getGameMode(),game.getMapName(),game.getMap(),game.getCurrentEvent(),game.getPlayerTeam(),game.getPlayersList(),game.getWorld(),game.getLastTime());
    }

    // --- IGame impl ---
    public UUID getGAME_UUID() { return gameUuid; }
    public boolean isRunning() { return running; }
    public void stopRunning() {}
    public long getTime_left() { return timeLeft; }
    public void setTime_left(long timeLeft) {}
    public GameModeType getGameMode() { return gameMode; }
    public MapName getMapName() { return mapName; }
    public Shape getMap() { return map; }
    public EventType getCurrentEvent() { return currentEvent; }
    public void setCurrentEvent(EventType currentEvent) {}
    public HashMap<String, ArrayList<Player>> getPlayerTeam() { return playerTeam; }
    public ArrayList<Player> getPlayersList() { return playersList; }
    public World getWorld() { return null; }
    public long getLastTime() { return lastTime; }
    public void setLastTime(long lastTime) {}
    public ArrayList<EntitySnapshot> getEntities() {
        return entities;
    }
}
