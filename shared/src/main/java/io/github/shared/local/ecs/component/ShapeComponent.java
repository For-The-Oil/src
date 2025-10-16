package io.github.shared.local.ecs.component;

import com.badlogic.ashley.core.Component;

import io.github.shared.local.data.gameobject.Cell;

public class ShapeComponent implements Component {
    private final int width;
    private final int height;
    private final Cell[][] tab_cells;

    public ShapeComponent(int width, int height) {
        this.width = width;
        this.height = height;
        this.tab_cells = new Cell[width][height];
    }

}
