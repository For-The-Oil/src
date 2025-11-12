package io.github.shared.local.data.instructions;

import io.github.shared.local.data.EnumsTypes.InstructionType;

public class Instruction {
    private long timestamp;
    private InstructionType update;

    public Instruction() {}
    public Instruction(long timestamp) {
        this.timestamp = timestamp;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
