package io.github.shared.data.requests;

import java.util.HashMap;

import io.github.shared.data.enumsTypes.SyncType;

public class SynchronizeRequest extends Request{
    private SyncType  type;
    private HashMap<String, Object> map;
    public SynchronizeRequest() {
        super();
    }
    public SynchronizeRequest(SyncType type, HashMap<String, Object> map){
        super();
        this.type = type;
        this.map = map;
    }

    public SyncType getType() {
        return type;
    }

    public HashMap<String, Object> getMap() {
        return map;
    }

}
