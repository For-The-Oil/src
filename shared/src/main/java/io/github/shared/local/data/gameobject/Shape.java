package io.github.shared.local.data.gameobject;

import com.artemis.Component;

public class Shape {
    private final int width;
    private final int height;
    private final Cell[][] tab_cells;

    public Shape(int width, int height) {
        this.width = width;
        this.height = height;
        this.tab_cells = new Cell[width][height];
    }

}
