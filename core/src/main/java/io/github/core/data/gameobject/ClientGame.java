package io.github.core.data.gameobject;

import com.artemis.Entity;
import com.artemis.World;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

import io.github.shared.local.data.EnumsTypes.EventType;
import io.github.shared.local.data.EnumsTypes.GameModeType;
import io.github.shared.local.data.EnumsTypes.MapName;
import io.github.shared.local.data.IGame;
import io.github.shared.local.data.gameobject.Shape;
import io.github.shared.local.data.network.Player;

public class ClientGame implements IGame {
    private final UUID GAME_UUID;
    private boolean running;
    private final World world;
    private final HashMap<String, ArrayList<Player>> playerTeam;
    private final ArrayList<Player> playersList;
    private final GameModeType gameMode;
    private Shape map;
    private MapName mapName;
    private EventType currentEvent;
    private long lastTime;
    private long timeLeft;  // seconds

    public ClientGame(GameModeType gameMode, MapName mapName, Shape map) {
        this.GAME_UUID = UUID.randomUUID();
        this.world = new World();System.err.print("Attention à changer new World();");//Attention à changer
        this.gameMode = gameMode;
        this.mapName = mapName;
        this.map = map;
        this.playerTeam = new HashMap<>();
        this.playersList = new ArrayList<>();
        this.running = true;
        this.timeLeft = 0;
        this.lastTime = System.currentTimeMillis();
        this.currentEvent = EventType.START;
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



}
