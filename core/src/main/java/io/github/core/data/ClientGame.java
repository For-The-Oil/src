package io.github.core.data;

import com.artemis.World;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;

import io.github.core.game_engine.EcsClientGame;
import io.github.core.game_engine.factory.ModelFactory;
import io.github.shared.data.enums_types.EventType;
import io.github.shared.data.enums_types.GameModeType;
import io.github.shared.data.enums_types.MapName;
import io.github.shared.data.IGame;
import io.github.shared.data.gameobject.Shape;
import io.github.shared.data.instructions.Instruction;
import io.github.shared.data.network.Player;

public class ClientGame implements IGame {

    private static ClientGame INSTANCE;
    private final UUID GAME_UUID;
    private boolean running;
    private final World world;
    private final HashMap<String, ArrayList<Player>> playerTeam;
    private final ArrayList<Player> playersList;
    private final GameModeType gameMode;
    private boolean isMapDirty;
    private Shape map;
    private MapName mapName;
    private EventType currentEvent;
    private final Queue<Instruction> executionQueue;
    private final Queue<ExtendedModelInstance> ModelInstanceQueue;
    private float accumulator;
    private long lastTime;
    private long timeLeft;  // seconds

    private ClientGame(GameModeType gameMode, MapName mapName, Shape map, UUID uuid, long timeLeft) {
        this.GAME_UUID = uuid;
        this.gameMode = gameMode;
        this.mapName = mapName;
        this.map = map;
        this.isMapDirty = true;
        this.playerTeam = new HashMap<>();
        this.playersList = new ArrayList<>();
        this.running = true;
        this.executionQueue = new ConcurrentLinkedQueue<>();
        this.ModelInstanceQueue = new ConcurrentLinkedQueue<>();
        this.accumulator = 0f;
        this.lastTime = System.currentTimeMillis();
        this.timeLeft = timeLeft;
        this.currentEvent = EventType.START;

        this.world = new World(EcsClientGame.serverWorldConfiguration(ModelInstanceQueue));// Important this line after anything else because dangerous overwise
    }

    public static ClientGame getInstance() {
        if (INSTANCE == null){
            throw new NullPointerException("The ClientGame instance is null");
        }
        return INSTANCE;
    }

    public static void setInstance(GameModeType gameMode, MapName mapName, Shape map, UUID uuid, long timeLeft) {
        INSTANCE = new ClientGame(gameMode,mapName,map,uuid,timeLeft);
    }

    public static void removeInstance() {
        INSTANCE = null;
    }

    public static boolean isInstanceNull() {
        return INSTANCE == null;
    }


    @Override
    public UUID getGAME_UUID() {
        return GAME_UUID;
    }

    @Override
    public boolean isRunning() {
        return running;
    }

    @Override
    public void stopRunning() {
        this.running = false;
    }

    // Setter ajouté pour running
    public void setRunning(boolean running) {
        this.running = running;
    }

    @Override
    public long getTime_left() {
        return timeLeft;
    }

    @Override
    public void setTime_left(long timeLeft) {
        this.timeLeft = timeLeft;
    }

    @Override
    public GameModeType getGameMode() {
        return gameMode;
    }

    @Override
    public MapName getMapName() {
        return mapName;
    }

    // Setter ajouté pour mapName
    public void setMapName(MapName mapName) {
        this.mapName = mapName;
    }

    @Override
    public Shape getMap() {
        return map;
    }

    @Override
    public boolean isMapDirty() {
        return isMapDirty;
    }

    @Override
    public void setMapDirty(boolean dirty) {
        isMapDirty = dirty;
    }

    // Setter ajouté pour map
    public void setMap(Shape map) {
        this.map = map;
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
    public HashMap<String, ArrayList<Player>> getPlayerTeam() {
        return playerTeam;
    }

    // Setter ajouté pour playerTeam
    public void setPlayerTeam(HashMap<String, ArrayList<Player>> playerTeam) {
        this.playerTeam.clear();
        if (playerTeam != null) {
            this.playerTeam.putAll(playerTeam);
        }
    }

    @Override
    public ArrayList<Player> getPlayersList() {
        return playersList;
    }

    // Setter ajouté pour playersList
    public void setPlayersList(ArrayList<Player> playersList) {
        this.playersList.clear();
        if (playersList != null) {
            this.playersList.addAll(playersList);
        }
    }

    @Override
    public World getWorld() {
        return world;
    }

    @Override
    public long getLastTime() {
        return lastTime;
    }

    @Override
    public void setLastTime(long lastTime) {
        this.lastTime = lastTime;
    }


    public float getAccumulator() {
        return accumulator;
    }

    public void setAccumulator(float accumulator) {
        this.accumulator = accumulator;
    }

    public Queue<Instruction> getExecutionQueue() {
        return executionQueue;
    }

    public boolean isEmptyExecutionQueue() {
        return executionQueue.isEmpty();
    }

    public void addQueueInstruction(Collection<Instruction> instruction){
        executionQueue.addAll(instruction);
    }

    public Queue<ExtendedModelInstance> getModelInstanceQueue() {
        return ModelInstanceQueue;
    }
}
