package io.github.server.data;

import com.artemis.Entity;

import java.util.Queue;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

import io.github.server.game_engine.ActionController.ActionController;
import io.github.shared.local.data.instructions.Instruction;
import io.github.shared.local.data.EnumsTypes.EventType;
import io.github.shared.local.data.EnumsTypes.MapName;
import io.github.shared.local.data.network.Player;
import io.github.shared.local.data.gameobject.Shape;

public class Game {
    private final UUID GAME_UUID;
    private boolean endGame;
    private HashMap<String, ArrayList<Player> > playerTeam;
    private ArrayList<Player> playersList;
    private ArrayList<Entity> entities;
    private final Shape initMap;
    private Shape map;
    private MapName mapName;
    private EventType currentEvent;
    private Queue<Instruction> executionQueue;
    private Queue<Instruction> historicQueue;
    private Queue<Instruction> networkQueue;
    private ArrayList<ActionController> activeActions;
    private long time_left;  //seconds

    public Game(UUID gameUuid, HashMap<String, ArrayList<Player>> playerTeam, ArrayList<Player> playersList, Shape map, EventType currentEvent, long timeLeft) {
        GAME_UUID = gameUuid;
        this.playerTeam = playerTeam;
        this.playersList = playersList;
        this.currentEvent = currentEvent;
        this.time_left = timeLeft;
        this.activeActions = new ArrayList<>();
        this.entities = new ArrayList<>();
        this.endGame = false;
        this.initMap = map;
        this.map = map;
    }
}
