package io.github.shared.local.data.component;

import io.github.shared.local.data.gameobject.Cell;

public class ShapeComponent {
    private final int width;
    private final int height;
    private final Cell[][] cells;

    public ShapeComponent(int width, int height) {
        this.width = width;
        this.height = height;
        this.cells = new Cell[width][height];
    }

    public void setCell(int x, int y, Cell cell) {
        cells[x][y] = cell;
    }

    public Cell getCell(int x, int y) {
        return cells[x][y];
    }

    public int getWidth() { return width; }
    public int getHeight() { return height; }
}
