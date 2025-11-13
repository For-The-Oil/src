package io.github.server.data;

import com.artemis.Entity;
import com.artemis.World;

import java.util.Queue;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;

import io.github.server.game_engine.ActionController.ActionController;
import io.github.server.game_engine.EcsServerEngine;
import io.github.shared.local.data.EnumsTypes.EntityType;
import io.github.shared.local.data.EnumsTypes.GameModeType;
import io.github.shared.local.data.IGame;
import io.github.shared.local.data.instructions.CreateInstruction;
import io.github.shared.local.data.instructions.DestroyInstruction;
import io.github.shared.local.data.instructions.Instruction;
import io.github.shared.local.data.EnumsTypes.EventType;
import io.github.shared.local.data.EnumsTypes.MapName;
import io.github.shared.local.data.network.Player;
import io.github.shared.local.data.gameobject.Shape;
import io.github.server.game_engine.SnapshotTracker;
import io.github.shared.local.data.requests.Request;

public class ServerGame implements IGame {
    private final UUID GAME_UUID;
    private boolean running;
    private final World world; // Artémis ECS
    private HashMap<String,ArrayList<Player>> playerTeam;
    private ArrayList<Player> playersList;
    private final GameModeType gameMode;
    private Shape map;
    private MapName mapName;
    private EventType currentEvent;
    private final SnapshotTracker updateTracker;
    private CreateInstruction createTracker;
    private DestroyInstruction destroyTracker;
    private final Queue<Request> requestQueue;
    private final Queue<Instruction> executionQueue;
    private final Queue<Instruction> historicQueue;
    private final Queue<Instruction> networkQueue;
    private final ArrayList<ActionController> activeActions;
    private float accumulator;
    private long lastTime;
    private long time_left;  //seconds

    public ServerGame(UUID gameUuid, HashMap<String, ArrayList<Player>> playerTeam, ArrayList<Player> playersList, GameModeType gameMode, MapName mapName, EventType currentEvent, long timeLeft) {
        GAME_UUID = gameUuid;
        this.mapName = mapName;
        this.updateTracker = new SnapshotTracker();
        this.createTracker = new CreateInstruction();
        this.destroyTracker = new DestroyInstruction();
        this.playerTeam = playerTeam;
        this.playersList = playersList;
        this.gameMode = gameMode;
        this.currentEvent = currentEvent;
        this.requestQueue = new ConcurrentLinkedQueue<>();
        this.executionQueue = new ConcurrentLinkedQueue<>();
        this.historicQueue = new ConcurrentLinkedQueue<>();
        this.networkQueue = new ConcurrentLinkedQueue<>();
        this.accumulator = 0f;
        this.time_left = timeLeft;
        this.activeActions = new ArrayList<>();
        this.running = true;
        this.map = new Shape(mapName.getShapeType().getShape()); // deep copy via constructeur

        this.world = new World(EcsServerEngine.serverWorldConfiguration(this));// Important this line after anything else because dangerous overwise
    }

    @Override
    public UUID getGAME_UUID() {
        return GAME_UUID;
    }

    @Override
    public HashMap<String, ArrayList<Player>> getPlayerTeam() {
        return playerTeam;
    }

    @Override
    public ArrayList<Player> getPlayersList() {
        return playersList;
    }


    @Override
    public Shape getMap() {
        return map;
    }

    @Override
    public EventType getCurrentEvent() {
        return currentEvent;
    }

    @Override
    public void setCurrentEvent(EventType currentEvent) {
        this.currentEvent = currentEvent;
    }

    @Override
    public MapName getMapName() {
        return mapName;
    }

    public SnapshotTracker getUpdateTracker() {
        return updateTracker;
    }

    public ArrayList<ActionController> getActiveActions() {
        return activeActions;
    }

    @Override
    public long getTime_left() {
        return time_left;
    }

    @Override
    public void setTime_left(long time_left) {
        this.time_left = time_left;
    }

    @Override
    public GameModeType getGameMode() {
        return gameMode;
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

    public void addQueueInstruction(Instruction instruction){
        executionQueue.add(instruction);
        historicQueue.add(instruction);
        networkQueue.add(instruction);
    }

    public boolean isEmptyExecutionQueue() {
        return executionQueue.isEmpty();
    }

    public boolean isEmptyNetworkQueue() {
        return networkQueue.isEmpty();
    }

    @Override
    public boolean isRunning() {
        return running;
    }

    @Override
    public void stopRunning() {
        this.running = false;
    }

    @Override
    public World getWorld() {
        return world;
    }

    public float getAccumulator() {
        return accumulator;
    }

    public void setAccumulator(float accumulator) {
        this.accumulator = accumulator;
    }

    @Override
    public long getLastTime() {
        return lastTime;
    }

    @Override
    public void setLastTime(long lastTime) {
        this.lastTime = lastTime;
    }

    public Queue<Request> getRequestQueue() {
        return requestQueue;
    }

    public CreateInstruction consumeCreateInstruction(long timestamp) {
        CreateInstruction tmp = createTracker;
        createTracker = new CreateInstruction();
        tmp.setTimestamp(timestamp);
        return tmp;
    }

    public void addCreateInstruction(EntityType type, int netId, int from, int posX, int posY, UUID player) {
        createTracker.add(type,netId,from,posX,posY,player);
    }

    public DestroyInstruction consumeDestroyInstruction(long timestamp) {
        DestroyInstruction tmp = destroyTracker;
        destroyTracker = new DestroyInstruction();
        tmp.setTimestamp(timestamp);
        return tmp;
    }

    public void addDestroyInstruction(int netIdToKill) {
        destroyTracker.getToKill().add(netIdToKill);
    }
}
