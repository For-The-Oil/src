package io.github.shared.local.data.gameobject;

import com.artemis.Component;

public class Shape {
    private final int width;
    private final int height;
    private final Cell[][] tab_cells;

    public Shape(Cell[][] tab_cells) {
        this.tab_cells = tab_cells;
        this.height = tab_cells.length;
        this.width = tab_cells[0].length;
    }

    // Copy constructor

    public Shape(Shape other) {
        if (other == null) {
            this.width = 0;
            this.height = 0;
            this.tab_cells = new Cell[0][0];
            return;
        }
        this.width  = other.getWidth();
        this.height = other.getHeight();
        this.tab_cells = new Cell[this.height][this.width];
        for (int y = 0; y < this.height; y++) {
            for (int x = 0; x < this.width; x++) {
                Cell src = other.getCells(x, y);
                this.tab_cells[y][x] = (src != null) ? new Cell(src) : null;
            }
        }
    }

    public boolean isValidPosition(int x, int y) {
        return x >= 0 && y >= 0 && x < width && y < height;
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
        return tab_cells[y][x];
    }

}
