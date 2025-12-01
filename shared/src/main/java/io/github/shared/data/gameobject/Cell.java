package io.github.shared.data.gameobject;

import io.github.shared.data.enumsTypes.CellEffectType;
import io.github.shared.data.enumsTypes.CellType;


/**
 * Class that represents a cell on the 2D world map.
 * It has an enum CellType,
 * and some booleans that tell us how does it behave.
 */
public class Cell {
    private CellType cellType;
    private CellEffectType effectType;
    private Integer netId;

    public Cell(){}
    public Cell(CellType cellType) {
        this.cellType = cellType;
        this.effectType = CellEffectType.NONE;
        this.netId = null;
    }

    // Copy constructor
    public Cell(Cell other) {
        if (other == null) {
            this.cellType = CellType.VOID;
            this.effectType = CellEffectType.NONE;
            this.netId = null;
        } else {
            this.cellType = other.cellType;
            this.effectType = other.effectType;
            this.netId = other.netId;
        }
    }

    public Cell(Cell other,int netId) {
        if (other == null) {
            this.cellType = CellType.VOID;
            this.effectType = CellEffectType.NONE;
            this.netId = null;
        } else {
            this.cellType = other.cellType;
            this.effectType = other.effectType;
            this.netId = netId;
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

    public Integer getNetId() {
        return netId;
    }

    public void setNetId(Integer netId) {
        this.netId = netId;
    }

    public boolean isBreakable() {
        return cellType.isBreakable()|| this.netId != null;
    }
}
