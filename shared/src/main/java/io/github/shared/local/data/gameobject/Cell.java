package io.github.shared.local.data.gameobject;

import io.github.shared.local.data.EnumsTypes.CellEffectType;
import io.github.shared.local.data.EnumsTypes.CellType;


/**
 * Class that represents a cell on the 2D world map.
 * It has an enum CellType,
 * and some booleans that tell us how does it behave.
 */
public class Cell {
    private CellType cellType;
    private CellEffectType effectType;

    public Cell(CellType cellType) {
        this.cellType = cellType;
        this.effectType = CellEffectType.NONE;
    }

    // Copy constructor
    public Cell(Cell other) {
        if (other == null) {
            this.cellType = CellType.VOID;
            this.effectType = CellEffectType.NONE;
        } else {
            this.cellType = other.cellType;
            this.effectType = other.effectType;
        }
    }



    public CellType getCellType() {
        return cellType;
    }

    public void setCellType(CellType cellType) {
        this.cellType = cellType;
    }

    public CellEffectType getEffectType() {
        return effectType;
    }

    public void setEffectType(CellEffectType effectType) {
        this.effectType = effectType;
    }
}
