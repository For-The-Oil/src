package io.github.shared.local.data.updates;

import io.github.shared.local.data.EnumsTypes.EventType;

public class EventUpdate extends Update{

    private EventType type;


    public EventUpdate(){}
    public EventUpdate(long timestamp, EventType type) {
        super(timestamp);
        this.type = type;
    }
}
