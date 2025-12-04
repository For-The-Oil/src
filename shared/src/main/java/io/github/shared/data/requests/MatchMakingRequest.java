package io.github.shared.data.requests;

import java.util.HashMap;

import io.github.shared.data.enums_types.GameModeType;
import io.github.shared.data.enums_types.MatchModeType;

public class MatchMakingRequest extends Request{

    private MatchModeType command;
    private GameModeType gameMode;
    private HashMap<String, String> keys;

    public MatchMakingRequest() {
        super();
        keys = new HashMap<>();
    }
    public MatchMakingRequest(MatchModeType command, GameModeType gameMode, HashMap<String, String> keys) {
        super();
        this.command = command;
        this.gameMode = gameMode;
        this.keys = keys;
    }



    public HashMap<String, String> getKeys() {
        return keys;
    }

    public void setKeys(HashMap<String, String> keys) {
        this.keys = keys;
    }

    public MatchModeType getCommand() {
        return command;
    }

    public GameModeType getGameMode() {
        return gameMode;
    }
}
