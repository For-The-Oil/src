package io.github.shared.local.data.requests;

import io.github.shared.local.data.EnumsTypes.RequestType;

/**
 * Représente un ordre donné par un joueur, avec des paramètres obligatoires selon le type.
 */
public class Request {
    private long timestamp;
    private RequestType request;

    public Request(){}
    public Request(RequestType request) {
        this.request = request;
        this.timestamp= -1;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long x){this.timestamp=x;}

}
