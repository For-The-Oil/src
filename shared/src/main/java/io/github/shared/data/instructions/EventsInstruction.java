package io.github.shared.data.instructions;

import io.github.shared.data.enums_types.EventType;

public class EventsInstruction extends Instruction{
    private EventType type;

    public EventsInstruction(){}
    public EventsInstruction(long timestamp, EventType type) {
        super(timestamp);
        this.type = type;
    }

    public EventType getEventType() {
        return type;
    }

    public void setType(EventType type) {
        this.type = type;
    }
}
