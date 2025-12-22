package io.github.shared.data.enums_types;

import static io.github.shared.data.enums_types.CellType.*;

import java.util.ArrayList;
import java.util.Collections;

import io.github.shared.data.gameobject.Cell;
import io.github.shared.data.gameobject.Shape;

public enum ShapeType {
//    test(new Shape(new Cell[][]{
//        {new Cell(ROAD),new Cell(ROAD),new Cell(ROAD)},
//        {new Cell(ROAD),new Cell(VOID),new Cell(ROAD)},
//        {new Cell(WATER),new Cell(ROAD),new Cell(GRASS)},
//    }), new ArrayList<>()),

    test(new Shape(new Cell[][]{
        {new Cell(GRASS),new Cell(GRASS),new Cell(GRASS),new Cell(GRASS),new Cell(GRASS),new Cell(GRASS),new Cell(GRASS),new Cell(GRASS),new Cell(GRASS),new Cell(GRASS),new Cell(GRASS),new Cell(GRASS),new Cell(GRASS),new Cell(GRASS),new Cell(GRASS),new Cell(ROAD),new Cell(ROAD),new Cell(ROAD),new Cell(ROAD),new Cell(ROAD),new Cell(ROAD),new Cell(ROAD),new Cell(ROAD),new Cell(ROAD),new Cell(ROAD),new Cell(ROAD),new Cell(ROAD),new Cell(ROAD),new Cell(ROAD),new Cell(ROAD)},
        {new Cell(GRASS),new Cell(GRASS),new Cell(GRASS),new Cell(GRASS),new Cell(GRASS),new Cell(GRASS),new Cell(GRASS),new Cell(GRASS),new Cell(GRASS),new Cell(GRASS),new Cell(GRASS),new Cell(GRASS),new Cell(GRASS),new Cell(GRASS),new Cell(GRASS),new Cell(ROAD),new Cell(ROAD),new Cell(ROAD),new Cell(ROAD),new Cell(ROAD),new Cell(ROAD),new Cell(ROAD),new Cell(ROAD),new Cell(ROAD),new Cell(ROAD),new Cell(ROAD),new Cell(ROAD),new Cell(ROAD),new Cell(ROAD),new Cell(ROAD)},
        {new Cell(GRASS),new Cell(GRASS),new Cell(GRASS),new Cell(GRASS),new Cell(GRASS),new Cell(GRASS),new Cell(GRASS),new Cell(GRASS),new Cell(GRASS),new Cell(GRASS),new Cell(GRASS),new Cell(GRASS),new Cell(GRASS),new Cell(GRASS),new Cell(GRASS),new Cell(ROAD),new Cell(ROAD),new Cell(ROAD),new Cell(ROAD),new Cell(ROAD),new Cell(ROAD),new Cell(ROAD),new Cell(ROAD),new Cell(ROAD),new Cell(ROAD),new Cell(ROAD),new Cell(ROAD),new Cell(ROAD),new Cell(ROAD),new Cell(ROAD)},
        {new Cell(GRASS),new Cell(GRASS),new Cell(GRASS),new Cell(GRASS),new Cell(GRASS),new Cell(GRASS),new Cell(GRASS),new Cell(GRASS),new Cell(GRASS),new Cell(GRASS),new Cell(GRASS),new Cell(GRASS),new Cell(GRASS),new Cell(GRASS),new Cell(GRASS),new Cell(ROAD),new Cell(ROAD),new Cell(ROAD),new Cell(ROAD),new Cell(ROAD),new Cell(ROAD),new Cell(ROAD),new Cell(ROAD),new Cell(ROAD),new Cell(ROAD),new Cell(ROAD),new Cell(ROAD),new Cell(ROAD),new Cell(ROAD),new Cell(ROAD)},
        {new Cell(GRASS),new Cell(GRASS),new Cell(GRASS),new Cell(GRASS),new Cell(GRASS),new Cell(GRASS),new Cell(GRASS),new Cell(GRASS),new Cell(GRASS),new Cell(GRASS),new Cell(GRASS),new Cell(GRASS),new Cell(GRASS),new Cell(GRASS),new Cell(GRASS),new Cell(ROAD),new Cell(ROAD),new Cell(ROAD),new Cell(ROAD),new Cell(ROAD),new Cell(ROAD),new Cell(ROAD),new Cell(ROAD),new Cell(ROAD),new Cell(ROAD),new Cell(ROAD),new Cell(ROAD),new Cell(ROAD),new Cell(ROAD),new Cell(ROAD)},
        {new Cell(GRASS),new Cell(GRASS),new Cell(GRASS),new Cell(GRASS),new Cell(GRASS),new Cell(GRASS),new Cell(GRASS),new Cell(GRASS),new Cell(GRASS),new Cell(GRASS),new Cell(GRASS),new Cell(GRASS),new Cell(GRASS),new Cell(GRASS),new Cell(GRASS),new Cell(ROAD),new Cell(ROAD),new Cell(ROAD),new Cell(ROAD),new Cell(ROAD),new Cell(ROAD),new Cell(ROAD),new Cell(ROAD),new Cell(ROAD),new Cell(ROAD),new Cell(ROAD),new Cell(ROAD),new Cell(ROAD),new Cell(ROAD),new Cell(ROAD)},
        {new Cell(GRASS),new Cell(GRASS),new Cell(GRASS),new Cell(GRASS),new Cell(GRASS),new Cell(GRASS),new Cell(GRASS),new Cell(GRASS),new Cell(GRASS),new Cell(GRASS),new Cell(GRASS),new Cell(GRASS),new Cell(GRASS),new Cell(GRASS),new Cell(GRASS),new Cell(ROAD),new Cell(ROAD),new Cell(ROAD),new Cell(ROAD),new Cell(ROAD),new Cell(ROAD),new Cell(ROAD),new Cell(ROAD),new Cell(ROAD),new Cell(ROAD),new Cell(ROAD),new Cell(ROAD),new Cell(ROAD),new Cell(ROAD),new Cell(ROAD)},
        {new Cell(GRASS),new Cell(GRASS),new Cell(GRASS),new Cell(GRASS),new Cell(GRASS),new Cell(GRASS),new Cell(GRASS),new Cell(GRASS),new Cell(GRASS),new Cell(GRASS),new Cell(GRASS),new Cell(GRASS),new Cell(GRASS),new Cell(GRASS),new Cell(GRASS),new Cell(ROAD),new Cell(ROAD),new Cell(ROAD),new Cell(ROAD),new Cell(ROAD),new Cell(ROAD),new Cell(ROAD),new Cell(ROAD),new Cell(ROAD),new Cell(ROAD),new Cell(ROAD),new Cell(ROAD),new Cell(ROAD),new Cell(ROAD),new Cell(ROAD)},
        {new Cell(GRASS),new Cell(GRASS),new Cell(GRASS),new Cell(GRASS),new Cell(GRASS),new Cell(GRASS),new Cell(GRASS),new Cell(GRASS),new Cell(GRASS),new Cell(GRASS),new Cell(GRASS),new Cell(GRASS),new Cell(GRASS),new Cell(GRASS),new Cell(GRASS),new Cell(ROAD),new Cell(ROAD),new Cell(ROAD),new Cell(ROAD),new Cell(ROAD),new Cell(ROAD),new Cell(ROAD),new Cell(ROAD),new Cell(ROAD),new Cell(ROAD),new Cell(ROAD),new Cell(ROAD),new Cell(ROAD),new Cell(ROAD),new Cell(ROAD)},
        {new Cell(GRASS),new Cell(GRASS),new Cell(GRASS),new Cell(GRASS),new Cell(GRASS),new Cell(GRASS),new Cell(GRASS),new Cell(GRASS),new Cell(GRASS),new Cell(GRASS),new Cell(GRASS),new Cell(GRASS),new Cell(GRASS),new Cell(GRASS),new Cell(GRASS),new Cell(ROAD),new Cell(ROAD),new Cell(ROAD),new Cell(ROAD),new Cell(ROAD),new Cell(ROAD),new Cell(ROAD),new Cell(ROAD),new Cell(ROAD),new Cell(ROAD),new Cell(ROAD),new Cell(ROAD),new Cell(ROAD),new Cell(ROAD),new Cell(ROAD)},
        {new Cell(GRASS),new Cell(GRASS),new Cell(GRASS),new Cell(GRASS),new Cell(GRASS),new Cell(GRASS),new Cell(GRASS),new Cell(GRASS),new Cell(GRASS),new Cell(GRASS),new Cell(GRASS),new Cell(GRASS),new Cell(GRASS),new Cell(GRASS),new Cell(GRASS),new Cell(ROAD),new Cell(ROAD),new Cell(ROAD),new Cell(ROAD),new Cell(ROAD),new Cell(ROAD),new Cell(ROAD),new Cell(ROAD),new Cell(ROAD),new Cell(ROAD),new Cell(ROAD),new Cell(ROAD),new Cell(ROAD),new Cell(ROAD),new Cell(ROAD)},
        {new Cell(GRASS),new Cell(GRASS),new Cell(GRASS),new Cell(GRASS),new Cell(GRASS),new Cell(GRASS),new Cell(GRASS),new Cell(GRASS),new Cell(GRASS),new Cell(GRASS),new Cell(GRASS),new Cell(GRASS),new Cell(GRASS),new Cell(GRASS),new Cell(GRASS),new Cell(ROAD),new Cell(ROAD),new Cell(ROAD),new Cell(ROAD),new Cell(ROAD),new Cell(ROAD),new Cell(ROAD),new Cell(ROAD),new Cell(ROAD),new Cell(ROAD),new Cell(ROAD),new Cell(ROAD),new Cell(ROAD),new Cell(ROAD),new Cell(ROAD)},
        {new Cell(GRASS),new Cell(GRASS),new Cell(GRASS),new Cell(GRASS),new Cell(GRASS),new Cell(GRASS),new Cell(GRASS),new Cell(GRASS),new Cell(GRASS),new Cell(GRASS),new Cell(GRASS),new Cell(GRASS),new Cell(GRASS),new Cell(GRASS),new Cell(GRASS),new Cell(ROAD),new Cell(ROAD),new Cell(ROAD),new Cell(ROAD),new Cell(ROAD),new Cell(ROAD),new Cell(ROAD),new Cell(ROAD),new Cell(ROAD),new Cell(ROAD),new Cell(ROAD),new Cell(ROAD),new Cell(ROAD),new Cell(ROAD),new Cell(ROAD)},
        {new Cell(GRASS),new Cell(GRASS),new Cell(GRASS),new Cell(GRASS),new Cell(GRASS),new Cell(GRASS),new Cell(GRASS),new Cell(GRASS),new Cell(GRASS),new Cell(GRASS),new Cell(GRASS),new Cell(GRASS),new Cell(GRASS),new Cell(GRASS),new Cell(GRASS),new Cell(ROAD),new Cell(ROAD),new Cell(ROAD),new Cell(ROAD),new Cell(ROAD),new Cell(ROAD),new Cell(ROAD),new Cell(ROAD),new Cell(ROAD),new Cell(ROAD),new Cell(ROAD),new Cell(ROAD),new Cell(ROAD),new Cell(ROAD),new Cell(ROAD)},
        {new Cell(GRASS),new Cell(GRASS),new Cell(GRASS),new Cell(GRASS),new Cell(GRASS),new Cell(GRASS),new Cell(GRASS),new Cell(GRASS),new Cell(GRASS),new Cell(GRASS),new Cell(GRASS),new Cell(GRASS),new Cell(GRASS),new Cell(GRASS),new Cell(GRASS),new Cell(ROAD),new Cell(ROAD),new Cell(ROAD),new Cell(ROAD),new Cell(ROAD),new Cell(ROAD),new Cell(ROAD),new Cell(ROAD),new Cell(ROAD),new Cell(ROAD),new Cell(ROAD),new Cell(ROAD),new Cell(ROAD),new Cell(ROAD),new Cell(ROAD)},
        {new Cell(WATER),new Cell(WATER),new Cell(WATER),new Cell(WATER),new Cell(WATER),new Cell(WATER),new Cell(WATER),new Cell(WATER),new Cell(WATER),new Cell(WATER),new Cell(WATER),new Cell(WATER),new Cell(WATER),new Cell(WATER),new Cell(WATER),new Cell(VOID),new Cell(VOID),new Cell(VOID),new Cell(VOID),new Cell(VOID),new Cell(VOID),new Cell(VOID),new Cell(VOID),new Cell(VOID),new Cell(VOID),new Cell(VOID),new Cell(VOID),new Cell(VOID),new Cell(VOID),new Cell(VOID)},
        {new Cell(WATER),new Cell(WATER),new Cell(WATER),new Cell(WATER),new Cell(WATER),new Cell(WATER),new Cell(WATER),new Cell(WATER),new Cell(WATER),new Cell(WATER),new Cell(WATER),new Cell(WATER),new Cell(WATER),new Cell(WATER),new Cell(WATER),new Cell(VOID),new Cell(VOID),new Cell(VOID),new Cell(VOID),new Cell(VOID),new Cell(VOID),new Cell(VOID),new Cell(VOID),new Cell(VOID),new Cell(VOID),new Cell(VOID),new Cell(VOID),new Cell(VOID),new Cell(VOID),new Cell(VOID)},
        {new Cell(WATER),new Cell(WATER),new Cell(WATER),new Cell(WATER),new Cell(WATER),new Cell(WATER),new Cell(WATER),new Cell(WATER),new Cell(WATER),new Cell(WATER),new Cell(WATER),new Cell(WATER),new Cell(WATER),new Cell(WATER),new Cell(WATER),new Cell(VOID),new Cell(VOID),new Cell(VOID),new Cell(VOID),new Cell(VOID),new Cell(VOID),new Cell(VOID),new Cell(VOID),new Cell(VOID),new Cell(VOID),new Cell(VOID),new Cell(VOID),new Cell(VOID),new Cell(VOID),new Cell(VOID)},
        {new Cell(WATER),new Cell(WATER),new Cell(WATER),new Cell(WATER),new Cell(WATER),new Cell(WATER),new Cell(WATER),new Cell(WATER),new Cell(WATER),new Cell(WATER),new Cell(WATER),new Cell(WATER),new Cell(WATER),new Cell(WATER),new Cell(WATER),new Cell(VOID),new Cell(VOID),new Cell(VOID),new Cell(VOID),new Cell(VOID),new Cell(VOID),new Cell(VOID),new Cell(VOID),new Cell(VOID),new Cell(VOID),new Cell(VOID),new Cell(VOID),new Cell(VOID),new Cell(VOID),new Cell(VOID)},
        {new Cell(WATER),new Cell(WATER),new Cell(WATER),new Cell(WATER),new Cell(WATER),new Cell(WATER),new Cell(WATER),new Cell(WATER),new Cell(WATER),new Cell(WATER),new Cell(WATER),new Cell(WATER),new Cell(WATER),new Cell(WATER),new Cell(WATER),new Cell(VOID),new Cell(VOID),new Cell(VOID),new Cell(VOID),new Cell(VOID),new Cell(VOID),new Cell(VOID),new Cell(VOID),new Cell(VOID),new Cell(VOID),new Cell(VOID),new Cell(VOID),new Cell(VOID),new Cell(VOID),new Cell(VOID)},
        {new Cell(WATER),new Cell(WATER),new Cell(WATER),new Cell(WATER),new Cell(WATER),new Cell(WATER),new Cell(WATER),new Cell(WATER),new Cell(WATER),new Cell(WATER),new Cell(WATER),new Cell(WATER),new Cell(WATER),new Cell(WATER),new Cell(WATER),new Cell(VOID),new Cell(VOID),new Cell(VOID),new Cell(VOID),new Cell(VOID),new Cell(VOID),new Cell(VOID),new Cell(VOID),new Cell(VOID),new Cell(VOID),new Cell(VOID),new Cell(VOID),new Cell(VOID),new Cell(VOID),new Cell(VOID)},
        {new Cell(WATER),new Cell(WATER),new Cell(WATER),new Cell(WATER),new Cell(WATER),new Cell(WATER),new Cell(WATER),new Cell(WATER),new Cell(WATER),new Cell(WATER),new Cell(WATER),new Cell(WATER),new Cell(WATER),new Cell(WATER),new Cell(WATER),new Cell(VOID),new Cell(VOID),new Cell(VOID),new Cell(VOID),new Cell(VOID),new Cell(VOID),new Cell(VOID),new Cell(VOID),new Cell(VOID),new Cell(VOID),new Cell(VOID),new Cell(VOID),new Cell(VOID),new Cell(VOID),new Cell(VOID)},
        {new Cell(WATER),new Cell(WATER),new Cell(WATER),new Cell(WATER),new Cell(WATER),new Cell(WATER),new Cell(WATER),new Cell(WATER),new Cell(WATER),new Cell(WATER),new Cell(WATER),new Cell(WATER),new Cell(WATER),new Cell(WATER),new Cell(WATER),new Cell(VOID),new Cell(VOID),new Cell(VOID),new Cell(VOID),new Cell(VOID),new Cell(VOID),new Cell(VOID),new Cell(VOID),new Cell(VOID),new Cell(VOID),new Cell(VOID),new Cell(VOID),new Cell(VOID),new Cell(VOID),new Cell(VOID)},
        {new Cell(WATER),new Cell(WATER),new Cell(WATER),new Cell(WATER),new Cell(WATER),new Cell(WATER),new Cell(WATER),new Cell(WATER),new Cell(WATER),new Cell(WATER),new Cell(WATER),new Cell(WATER),new Cell(WATER),new Cell(WATER),new Cell(WATER),new Cell(VOID),new Cell(VOID),new Cell(VOID),new Cell(VOID),new Cell(VOID),new Cell(VOID),new Cell(VOID),new Cell(VOID),new Cell(VOID),new Cell(VOID),new Cell(VOID),new Cell(VOID),new Cell(VOID),new Cell(VOID),new Cell(VOID)},
        {new Cell(WATER),new Cell(WATER),new Cell(WATER),new Cell(WATER),new Cell(WATER),new Cell(WATER),new Cell(WATER),new Cell(WATER),new Cell(WATER),new Cell(WATER),new Cell(WATER),new Cell(WATER),new Cell(WATER),new Cell(WATER),new Cell(WATER),new Cell(VOID),new Cell(VOID),new Cell(VOID),new Cell(VOID),new Cell(VOID),new Cell(VOID),new Cell(VOID),new Cell(VOID),new Cell(VOID),new Cell(VOID),new Cell(VOID),new Cell(VOID),new Cell(VOID),new Cell(VOID),new Cell(VOID)},
        {new Cell(WATER),new Cell(WATER),new Cell(WATER),new Cell(WATER),new Cell(WATER),new Cell(WATER),new Cell(WATER),new Cell(WATER),new Cell(WATER),new Cell(WATER),new Cell(WATER),new Cell(WATER),new Cell(WATER),new Cell(WATER),new Cell(WATER),new Cell(VOID),new Cell(VOID),new Cell(VOID),new Cell(VOID),new Cell(VOID),new Cell(VOID),new Cell(VOID),new Cell(VOID),new Cell(VOID),new Cell(VOID),new Cell(VOID),new Cell(VOID),new Cell(VOID),new Cell(VOID),new Cell(VOID)},
        {new Cell(WATER),new Cell(WATER),new Cell(WATER),new Cell(WATER),new Cell(WATER),new Cell(WATER),new Cell(WATER),new Cell(WATER),new Cell(WATER),new Cell(WATER),new Cell(WATER),new Cell(WATER),new Cell(WATER),new Cell(WATER),new Cell(WATER),new Cell(VOID),new Cell(VOID),new Cell(VOID),new Cell(VOID),new Cell(VOID),new Cell(VOID),new Cell(VOID),new Cell(VOID),new Cell(VOID),new Cell(VOID),new Cell(VOID),new Cell(VOID),new Cell(VOID),new Cell(VOID),new Cell(VOID)},
        {new Cell(WATER),new Cell(WATER),new Cell(WATER),new Cell(WATER),new Cell(WATER),new Cell(WATER),new Cell(WATER),new Cell(WATER),new Cell(WATER),new Cell(WATER),new Cell(WATER),new Cell(WATER),new Cell(WATER),new Cell(WATER),new Cell(WATER),new Cell(VOID),new Cell(VOID),new Cell(VOID),new Cell(VOID),new Cell(VOID),new Cell(VOID),new Cell(VOID),new Cell(VOID),new Cell(VOID),new Cell(VOID),new Cell(VOID),new Cell(VOID),new Cell(VOID),new Cell(VOID),new Cell(VOID)},
        {new Cell(WATER),new Cell(WATER),new Cell(WATER),new Cell(WATER),new Cell(WATER),new Cell(WATER),new Cell(WATER),new Cell(WATER),new Cell(WATER),new Cell(WATER),new Cell(WATER),new Cell(WATER),new Cell(WATER),new Cell(WATER),new Cell(WATER),new Cell(VOID),new Cell(VOID),new Cell(VOID),new Cell(VOID),new Cell(VOID),new Cell(VOID),new Cell(VOID),new Cell(VOID),new Cell(VOID),new Cell(VOID),new Cell(VOID),new Cell(VOID),new Cell(VOID),new Cell(VOID),new Cell(VOID)},
        {new Cell(WATER),new Cell(WATER),new Cell(WATER),new Cell(WATER),new Cell(WATER),new Cell(WATER),new Cell(WATER),new Cell(WATER),new Cell(WATER),new Cell(WATER),new Cell(WATER),new Cell(WATER),new Cell(WATER),new Cell(WATER),new Cell(WATER),new Cell(VOID),new Cell(VOID),new Cell(VOID),new Cell(VOID),new Cell(VOID),new Cell(VOID),new Cell(VOID),new Cell(VOID),new Cell(VOID),new Cell(VOID),new Cell(VOID),new Cell(VOID),new Cell(VOID),new Cell(VOID),new Cell(VOID)},
        }), new ArrayList<>()),

    Statue(new Shape(new Cell[][]{
        {new Cell(WATER),new Cell(WATER),new Cell(WATER),new Cell(WATER)},
        {new Cell(WATER),new Cell(ROAD),new Cell(ROAD),new Cell(WATER)},
        {new Cell(WATER),new Cell(ROAD),new Cell(ROAD),new Cell(WATER)},
        {new Cell(WATER),new Cell(WATER),new Cell(WATER),new Cell(WATER)}
    }), new ArrayList<>()),

    Base( new Shape(new Cell[][]{
        {new Cell(ROAD),new Cell(VOID), new Cell(VOID), new Cell(VOID), new Cell(ROAD)},
        {new Cell(VOID),new Cell(ROAD), new Cell(ROAD), new Cell(ROAD), new Cell(VOID)},
        {new Cell(VOID),new Cell(ROAD), new Cell(ROAD), new Cell(ROAD), new Cell(VOID)},
        {new Cell(VOID),new Cell(ROAD), new Cell(ROAD), new Cell(ROAD), new Cell(VOID)},
        {new Cell(ROAD),new Cell(VOID), new Cell(VOID), new Cell(VOID), new Cell(ROAD)}
    }), new ArrayList<>(Collections.singleton(GRASS))),
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
    }), new ArrayList<>()),

    MINE(new Shape(new Cell[][]{
        {new Cell(ROAD),new Cell(ROAD),new Cell(ROAD)},
        {new Cell(ROAD),new Cell(VOID),new Cell(ROAD)},
        {new Cell(ROAD),new Cell(ROAD),new Cell(ROAD)},
    }), new ArrayList<>()),

    DERRICK(new Shape(new Cell[][]{
        {new Cell(ROAD),new Cell(ROAD),new Cell(ROAD)},
        {new Cell(ROAD),new Cell(VOID),new Cell(ROAD)},
        {new Cell(ROAD),new Cell(ROAD),new Cell(ROAD)},
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
