package io.github.shared.local.data.gameobject;

import io.github.shared.local.data.nameEntity.CellType;


/**
 * Class that represents a cell on the 2D world map.
 * It has an enum CellType,
 * and some booleans that tell us how does it behave.
 */
public class Cell {
    private final CellType cellType;
    private final float walkable;
    private final float flyable;
    private final float swimmable;

    public Cell(CellType cellType, float walkable, float flyable, float swimmable) {
        this.cellType = cellType;
        this.walkable = walkable;
        this.flyable = flyable;
        this.swimmable = swimmable;
    }

    public CellType getCellType() { return cellType; }
    public float isWalkable() { return walkable; }

    public float isFlyable() {return flyable; }

    public float getSwimmable(){return swimmable;}


}
