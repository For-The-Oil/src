package io.github.shared.data.EnumsTypes;

import static io.github.shared.data.EnumsTypes.CellType.*;

import java.util.ArrayList;

import io.github.shared.data.gameobject.Cell;
import io.github.shared.data.gameobject.Shape;

public enum ShapeType {
    test(new Shape(new Cell[][]{
        {new Cell(ROAD),new Cell(ROAD),new Cell(ROAD)},
        {new Cell(ROAD),new Cell(ROAD),new Cell(VOID)},
        {new Cell(ROAD),new Cell(ROAD),new Cell(ROAD)},
    }),new ArrayList<>());

    //MAP1, MAP2,
    //Barrack, Factory, Garage;

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
