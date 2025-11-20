package io.github.shared.local.data.requests;

import java.util.HashMap;

import io.github.shared.local.data.EnumsTypes.DeckRequestType;
import io.github.shared.local.data.EnumsTypes.RequestType;

public class DeckRequest extends Request {

    private DeckRequestType mode;
    private HashMap<String, String> keys;

    public DeckRequest() {
        super(RequestType.DECK);
        keys = new HashMap<>();
    }

    public DeckRequest(DeckRequestType mode, HashMap<String, String> map ) {
        super(RequestType.DECK);
        keys = map;
    }


    public DeckRequestType getMode() {
        return mode;
    }

    public void setMode(DeckRequestType mode) {
        this.mode = mode;
    }

    public HashMap<String, String> getKeys() {
        return keys;
    }

    public void setKeys(HashMap<String, String> keys) {
        this.keys = keys;
    }
}
