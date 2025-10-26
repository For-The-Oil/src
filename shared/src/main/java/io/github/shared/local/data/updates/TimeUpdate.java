package io.github.shared.local.data.updates;

public class TimeUpdate extends Update{
    private long time_left; //in seconds

    public TimeUpdate(){}
    public TimeUpdate(long timestamp, long time_left) {
        super(timestamp);
        this.time_left = time_left;
    }
}
