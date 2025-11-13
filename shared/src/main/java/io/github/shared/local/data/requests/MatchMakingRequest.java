package io.github.shared.local.data.requests;

import java.util.HashMap;

import io.github.shared.local.data.EnumsTypes.GameModeType;
import io.github.shared.local.data.EnumsTypes.MatchModeType;
import io.github.shared.local.data.EnumsTypes.RequestType;

public class MatchMakingRequest extends Request{

    private MatchModeType command;
    private GameModeType gameMode;
    private HashMap<String, String> keys;

    public MatchMakingRequest() {
        super(RequestType.AUTH);
        keys = new HashMap<>();
    }
    public MatchMakingRequest(MatchModeType command, GameModeType gameMode, HashMap<String, String> keys) {
        super(RequestType.AUTH);
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
