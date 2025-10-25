package io.github.shared.local.data;

import java.io.Serializable;
import io.github.shared.local.data.nameEntity.RequestType;

/**
 * Représente un ordre donné par un joueur, avec des paramètres obligatoires selon le type.
 */
public class Request implements Serializable {
    private long timestamp;
    private final RequestType request;

    public Request(RequestType request) {
        this.request = request;
        this.timestamp= -1;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long x){
        this.timestamp=x;
    }

}
