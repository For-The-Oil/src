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
        if (x >= 0 && x < width && y >= 0 && y < height) {
            cells[x][y] = cell;
        }
        else {
            //LOG4J
        }
    }

    public Cell getCell(int x, int y) {
        if (x >= 0 && x < width && y >= 0 && y < height) {
            return cells[x][y];
        }
        else {
            //LOG4J
            return null;
        }
    }

    public int getWidth() { return width; }
    public int getHeight() { return height; }
}
