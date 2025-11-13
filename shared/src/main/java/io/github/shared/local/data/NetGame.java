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
    private UUID gameUuid;
    private boolean running;
    private long timeLeft;
    private GameModeType gameMode;
    private MapName mapName;
    private Shape map;
    private EventType currentEvent;
    private HashMap<String, ArrayList<Player>> playerTeam;
    private ArrayList<Player> playersList;
    private ArrayList<EntitySnapshot> entities;
    private long lastTime;

    public NetGame(){};

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
    public void setCurrentEvent(EventType currentEvent) { this.currentEvent = currentEvent ;}
    public HashMap<String, ArrayList<Player>> getPlayerTeam() { return playerTeam; }
    public ArrayList<Player> getPlayersList() { return playersList; }
    public World getWorld() { return null; }
    public long getLastTime() { return lastTime; }
    public void setLastTime(long lastTime) { this.lastTime = lastTime;}
    public ArrayList<EntitySnapshot> getEntities() {
        return entities;
    }


    public void setGameUuid(UUID gameUuid) {
        this.gameUuid = gameUuid;
    }

    public void setRunning(boolean running) {
        this.running = running;
    }

    public void setTimeLeft(long timeLeft) {
        this.timeLeft = timeLeft;
    }

    public void setGameMode(GameModeType gameMode) {
        this.gameMode = gameMode;
    }

    public void setMapName(MapName mapName) {
        this.mapName = mapName;
    }

    public void setMap(Shape map) {
        this.map = map;
    }

    public void setPlayerTeam(HashMap<String, ArrayList<Player>> playerTeam) {
        this.playerTeam = playerTeam;
    }

    public void setPlayersList(ArrayList<Player> playersList) {
        this.playersList = playersList;
    }

    public void setEntities(ArrayList<EntitySnapshot> entities) {
        this.entities = entities;
    }


}
