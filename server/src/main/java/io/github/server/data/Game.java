package io.github.server.data;

import com.artemis.Entity;

import java.util.Queue;
import java.util.ArrayList;
import java.util.HashMap;

import io.github.server.data.ActionController.ActionController;
import io.github.shared.local.data.instructions.Instruction;
import io.github.shared.local.data.EnumsTypes.EventType;
import io.github.shared.local.data.EnumsTypes.MapName;
import io.github.shared.local.data.Player;
import io.github.shared.local.data.gameobject.Shape;

public class Game {
    private long GAME_UUID;

    private boolean endGame;
    private HashMap<String, ArrayList<Player> > playerTeam;
    private ArrayList<Player> playersList;
    private ArrayList<Entity> entities;
    private Shape map;
    private MapName mapName;
    private EventType currentEvent;
    private Queue<Instruction> orderList;
    private Queue<Instruction> historicList;
    private ArrayList<ActionController> activeActions;
    private long time_left;  //seconds
}
