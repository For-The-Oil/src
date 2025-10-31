package io.github.shared.local.data.gameobject;

import io.github.shared.local.data.EnumsTypes.CellType;


/**
 * Class that represents a cell on the 2D world map.
 * It has an enum CellType,
 * and some booleans that tell us how does it behave.
 */
public class Cell {
    private CellType cellType;

    public Cell(CellType cellType) {
        this.cellType = cellType;
    }

    public CellType getCellType() {
        return cellType;
    }

    public void setCellType(CellType cellType) {
        this.cellType = cellType;
    }
}
