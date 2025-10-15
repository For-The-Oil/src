package io.github.shared.local.data.gameobject;

import io.github.shared.local.data.component.ShapeComponent;

public class Map {
    private final String idMap;
    private final ShapeComponent shape;

    public Map(String idMap, ShapeComponent shape) {
        this.idMap = idMap;
        this.shape = shape;
    }

    public String getIdMap() {
        return idMap;
    }

    public ShapeComponent getShape() {
        return shape;
    }
}
