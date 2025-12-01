package io.github.shared.data.network;

import io.github.shared.data.enumsTypes.KryoMessageType;

public class KryoMessage {
    private KryoMessageType type;
    private String token;

    private Object obj;

    public KryoMessage(){}

    public KryoMessage(KryoMessageType type, String token, Object obj) {
        this.type = type;
        this.token = token;
        this.obj = obj;
    }

    public KryoMessageType getType() {
        return type;
    }

    public void setType(KryoMessageType type) {
        this.type = type;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Object getObj() {
        return obj;
    }

    public void setObj(Object obj) {
        this.obj = obj;
    }
}
