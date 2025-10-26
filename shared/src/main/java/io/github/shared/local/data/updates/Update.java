package io.github.shared.local.data.updates;

import java.io.Serializable;

import io.github.shared.local.data.EnumsTypes.UpdateType;

public class Update {

    private long timestamp;
    private UpdateType update;

    public Update() {}

    public Update(long timestamp) {
        this.timestamp = timestamp;
    }



}
