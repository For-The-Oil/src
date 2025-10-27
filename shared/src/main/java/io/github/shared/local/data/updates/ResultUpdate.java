package io.github.shared.local.data.updates;

import java.util.HashMap;

public class ResultUpdate {
    private HashMap<String, String> result;
    private HashMap<String, Integer> stats;

    public ResultUpdate(){}
    public ResultUpdate(long timestamp, HashMap<String, String> result, HashMap<String, Integer> stats) {
        super(timestamp);
        this.result = result;
        this.stats = stats;
    }
}
