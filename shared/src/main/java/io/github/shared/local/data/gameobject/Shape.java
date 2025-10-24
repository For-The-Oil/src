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

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public Cell[][] getTab_cells() {
        return tab_cells;
    }

    public Cell getCells(int x, int y){
        return tab_cells[x][y];
    }

}
