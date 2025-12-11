package io.github.android.gui.animation;

import java.util.HashMap;
import java.util.Map;

public class DecisionTreeLoader {

    public interface StepCallback {
        void onSuccess();
        void onFailure();
    }

    public interface Step {
        void run(StepCallback callback);
    }

    private static class Node {
        Step step;
        String nextOnSuccess;
        String nextOnFailure;

        Node(Step step, String nextOnSuccess, String nextOnFailure) {
            this.step = step;
            this.nextOnSuccess = nextOnSuccess;
            this.nextOnFailure = nextOnFailure;
        }
    }

    private final Map<String, Node> nodes = new HashMap<>();
    private String startKey;

    public void addStep(String key, Step step, String nextOnSuccess, String nextOnFailure) {
        nodes.put(key, new Node(step, nextOnSuccess, nextOnFailure));
        if (startKey == null) startKey = key;
    }

    public void setStart(String key) {
        this.startKey = key;
    }

    public void start() {
        runNode(startKey);
    }

    private void runNode(String key) {
        if (key == null) return;
        Node node = nodes.get(key);
        if (node == null) return;

        node.step.run(new StepCallback() {
            @Override
            public void onSuccess() {
                runNode(node.nextOnSuccess);
            }

            @Override
            public void onFailure() {
                runNode(node.nextOnFailure);
            }
        });
    }
}
