package io.github.shared.data.requests;

import java.util.HashMap;

import io.github.shared.data.EnumsTypes.AuthModeType;

public class AuthRequest extends Request {

    private AuthModeType mode;
    private HashMap<String, String> keys;

    public AuthRequest() {
        super();
        keys = new HashMap<>();
    }
    public AuthRequest(AuthModeType mode, HashMap<String, String> keys) {
        super();
        this.mode = mode;
        this.keys = keys;
    }


    public AuthModeType getMode() {
        return mode;
    }

    public void setMode(AuthModeType mode) {
        this.mode = mode;
    }

    public HashMap<String, String> getKeys() {
        return keys;
    }

    public void setKeys(HashMap<String, String> keys) {
        this.keys = keys;
    }
}
