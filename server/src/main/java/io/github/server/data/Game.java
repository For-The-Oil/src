package io.github.server.data;

import com.artemis.Entity;

import java.util.Queue;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

import io.github.server.game_engine.ActionController.ActionController;
import io.github.shared.local.data.EnumsTypes.GameModeType;
import io.github.shared.local.data.EnumsTypes.ShapeType;
import io.github.shared.local.data.instructions.Instruction;
import io.github.shared.local.data.EnumsTypes.EventType;
import io.github.shared.local.data.EnumsTypes.MapName;
import io.github.shared.local.data.network.Player;
import io.github.shared.local.data.gameobject.Shape;
import io.github.server.game_engine.SnapshotTracker;

public class Game {
    private final UUID GAME_UUID;
    private boolean endGame;
    private HashMap<String, ArrayList<Player> > playerTeam;
    private ArrayList<Player> playersList;
    private ArrayList<Entity> entities;
    private final GameModeType gameMode;
    private final ShapeType mapType;
    private Shape map;
    private MapName mapName;
    private EventType currentEvent;
    private SnapshotTracker snapshotTracker;
    private Queue<Instruction> executionQueue;
    private Queue<Instruction> historicQueue;
    private Queue<Instruction> networkQueue;
    private ArrayList<ActionController> activeActions;
    private long time_left;  //seconds

    public Game(UUID gameUuid, HashMap<String, ArrayList<Player>> playerTeam, ArrayList<Player> playersList, GameModeType gameMode, ShapeType mapType, EventType currentEvent, long timeLeft) {
        GAME_UUID = gameUuid;
        this.playerTeam = playerTeam;
        this.playersList = playersList;
        this.gameMode = gameMode;
        this.currentEvent = currentEvent;
        this.time_left = timeLeft;
        this.activeActions = new ArrayList<>();
        this.entities = new ArrayList<>();
        this.endGame = false;
        this.mapType = mapType;
        this.map = new Shape(mapType.getShape().getTab_cells().clone());
    }

    public UUID getGAME_UUID() {
        return GAME_UUID;
    }

    public boolean isEndGame() {
        return endGame;
    }

    public HashMap<String, ArrayList<Player>> getPlayerTeam() {
        return playerTeam;
    }

    public ArrayList<Player> getPlayersList() {
        return playersList;
    }

    public ArrayList<Entity> getEntities() {
        return entities;
    }

    public ShapeType getMapType() {
        return mapType;
    }

    public Shape getMap() {
        return map;
    }

    public EventType getCurrentEvent() {
        return currentEvent;
    }

    public MapName getMapName() {
        return mapName;
    }

    public SnapshotTracker getSnapshotTracker() {
        return snapshotTracker;
    }

    public Queue<Instruction> getExecutionQueue() {
        return executionQueue;
    }

    public Queue<Instruction> getHistoricQueue() {
        return historicQueue;
    }

    public Queue<Instruction> getNetworkQueue() {
        return networkQueue;
    }

    public ArrayList<ActionController> getActiveActions() {
        return activeActions;
    }

    public long getTime_left() {
        return time_left;
    }

    public void setTime_left(long time_left) {
        this.time_left = time_left;
    }

    public GameModeType getGameMode() {
        return gameMode;
    }
}
