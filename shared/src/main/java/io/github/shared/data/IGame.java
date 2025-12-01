package io.github.shared.data;


import com.artemis.World;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

import io.github.shared.data.enumsTypes.EventType;
import io.github.shared.data.enumsTypes.GameModeType;
import io.github.shared.data.enumsTypes.MapName;
import io.github.shared.data.gameobject.Shape;
import io.github.shared.data.network.Player;

public interface IGame {
    UUID getGAME_UUID();
    boolean isRunning();
    void stopRunning();

    // Time management
    long getTime_left();
    void setTime_left(long timeLeft);

    // Game mode and map
    GameModeType getGameMode();
    MapName getMapName();
    Shape getMap();

    // Event handling
    EventType getCurrentEvent();
    void setCurrentEvent(EventType currentEvent);

    // Players and entities
    HashMap<String, ArrayList<Player>> getPlayerTeam();
    ArrayList<Player> getPlayersList();

    // ECS world
    World getWorld();

    long getLastTime();
    void setLastTime(long lastTime);
}

