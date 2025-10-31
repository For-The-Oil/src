package io.github.shared.local.data.snapshot;

import java.io.Serializable;
import java.util.HashMap;

public class ComponentSnapshot implements Serializable {
    private String type; // ex : "Position", "Health"
    private HashMap<String, Object> fields; // x, y, hp, etc.

    public ComponentSnapshot() {}
    public ComponentSnapshot(String type, HashMap<String, Object> fields) {
        this.type = type;
        this.fields = fields;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public HashMap<String, Object> getFields() {
        return fields;
    }

    public void setFields(HashMap<String, Object> fields) {
        this.fields = fields;
    }
}
