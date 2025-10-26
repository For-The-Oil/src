package io.github.shared.local.data.snapshot;

import java.io.Serializable;
import java.util.HashMap;

public class ComponentSnapshot implements Serializable {
    public String type; // ex : "Position", "Health"
    public HashMap<String, Object> fields; // x, y, hp, etc.
}
