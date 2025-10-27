package io.github.shared.local.data.instructions;

import io.github.shared.local.data.EnumsTypes.EventType;

public class EventsInstruction extends Instruction{
    private EventType type;
    public EventsInstruction(){}
    public EventsInstruction(long timestamp, EventType type) {
        super(timestamp);
        this.type = type;
    }
}
