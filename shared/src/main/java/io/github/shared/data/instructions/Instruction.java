package io.github.shared.data.instructions;

public class Instruction {
    private long timestamp;

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
