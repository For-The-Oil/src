package io.github.shared.local.data.requests;

import java.util.HashMap;

import io.github.shared.local.data.EnumsTypes.SyncType;

public class SynchronizeRequest extends Request{
    private SyncType  type;
    private HashMap<String, Object> map;
    public SynchronizeRequest() {}
    public SynchronizeRequest(SyncType type, HashMap<String, Object> map){
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
