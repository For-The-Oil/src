package io.github.shared.local.data.requests;

import java.util.HashMap;

import io.github.shared.local.data.EnumsTypes.AuthModeType;
import io.github.shared.local.data.EnumsTypes.RequestType;

public class AuthRequest extends Request {

    private AuthModeType mode;
    private HashMap<String, String> keys;

    public AuthRequest() {
        super(RequestType.AUTH);
        keys = new HashMap<>();
    }
    public AuthRequest(AuthModeType mode, HashMap<String, String> keys) {
        super(RequestType.AUTH);
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
