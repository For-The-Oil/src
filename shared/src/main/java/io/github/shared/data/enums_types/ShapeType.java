package io.github.shared.data.enums_types;

import static io.github.shared.data.enums_types.CellType.*;

import java.util.ArrayList;

import io.github.shared.data.gameobject.Cell;
import io.github.shared.data.gameobject.Shape;

public enum ShapeType {
    test(new Shape(new Cell[][]{
        {new Cell(ROAD),new Cell(ROAD),new Cell(ROAD)},
        {new Cell(ROAD),new Cell(VOID),new Cell(ROAD)},
        {new Cell(ROAD),new Cell(ROAD),new Cell(ROAD)},
    }), new ArrayList<>()),

    Base( new Shape(new Cell[][]{
        {new Cell(VOID),new Cell(VOID),new Cell(VOID),new Cell(VOID),new Cell(VOID),new Cell(VOID)},
        {new Cell(VOID),new Cell(VOID),new Cell(VOID),new Cell(VOID),new Cell(VOID),new Cell(VOID)},
        {new Cell(VOID),new Cell(VOID),new Cell(VOID),new Cell(VOID),new Cell(VOID),new Cell(VOID)},
        {new Cell(VOID),new Cell(VOID),new Cell(VOID),new Cell(VOID),new Cell(VOID),new Cell(VOID)},
        {new Cell(VOID),new Cell(VOID),new Cell(VOID),new Cell(VOID),new Cell(VOID),new Cell(VOID)},
        {new Cell(VOID),new Cell(VOID),new Cell(VOID),new Cell(VOID),new Cell(VOID),new Cell(VOID)},
    }), new ArrayList<>()),
    Barrack(new Shape(new Cell[][]{
        {new Cell(VOID),new Cell(VOID),new Cell(VOID),new Cell(VOID)},
        {new Cell(VOID),new Cell(VOID),new Cell(VOID),new Cell(VOID)},
        {new Cell(VOID),new Cell(VOID),new Cell(VOID),new Cell(VOID)},
        {new Cell(VOID),new Cell(VOID),new Cell(VOID),new Cell(VOID)}
    }), new ArrayList<>()),
    Factory(new Shape(new Cell[][]{
        {new Cell(VOID),new Cell(VOID),new Cell(VOID),new Cell(VOID), new Cell(VOID)},
        {new Cell(VOID),new Cell(VOID),new Cell(VOID),new Cell(VOID), new Cell(VOID)},
        {new Cell(VOID),new Cell(VOID),new Cell(VOID),new Cell(VOID), new Cell(VOID)},
        {new Cell(VOID),new Cell(VOID),new Cell(VOID),new Cell(VOID), new Cell(VOID)},
        {new Cell(VOID),new Cell(VOID),new Cell(VOID),new Cell(VOID), new Cell(VOID)}
    }), new ArrayList<>()),
    Garage(new Shape(new Cell[][]{
        {new Cell(VOID),new Cell(VOID),new Cell(VOID),new Cell(VOID), new Cell(VOID)},
        {new Cell(VOID),new Cell(VOID),new Cell(VOID),new Cell(VOID), new Cell(VOID)},
        {new Cell(VOID),new Cell(VOID),new Cell(VOID),new Cell(VOID), new Cell(VOID)},
        {new Cell(VOID),new Cell(VOID),new Cell(VOID),new Cell(VOID), new Cell(VOID)},
        {new Cell(VOID),new Cell(VOID),new Cell(VOID),new Cell(VOID), new Cell(VOID)}
    }), new ArrayList<>());

    private final Shape shape;
    private final ArrayList<CellType> canBePlacedOn;

    ShapeType(Shape shape, ArrayList<CellType> canBePlacedOn) {
        this.shape = shape;
        this.canBePlacedOn = canBePlacedOn;
    }

    public Shape getShape() {
        return shape;
    }

    public ArrayList<CellType> getCanBePlacedOn() {
        return canBePlacedOn;
    }
}
