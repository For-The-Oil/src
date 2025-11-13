package io.github.shared.local.data.instructions;

import java.util.HashMap;

public class FinalInstruction extends Instruction{
    private HashMap<String, String> result;
    private HashMap<String, Integer> stats;
    public FinalInstruction() {
        super();
    }

    public HashMap<String, String> getResult() {
        return result;
    }

    public void setResult(HashMap<String, String> result) {
        this.result = result;
    }

    public HashMap<String, Integer> getStats() {
        return stats;
    }

    public void setStats(HashMap<String, Integer> stats) {
        this.stats = stats;
    }
}
