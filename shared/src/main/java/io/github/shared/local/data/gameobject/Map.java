package io.github.shared.local.data.gameobject;

import io.github.shared.local.data.component.ShapeComponent;

public class Map {
    private final String name;
    private final ShapeComponent shape;

    public Map(String name, ShapeComponent shape) {
        this.name = name;
        this.shape = shape;
    }

    public String getName() {
        return name;
    }

    public ShapeComponent getShape() {
        return shape;
    }
}
