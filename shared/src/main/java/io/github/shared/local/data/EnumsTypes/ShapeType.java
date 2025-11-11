package io.github.shared.local.data.EnumsTypes;

import static io.github.shared.local.data.EnumsTypes.CellType.*;

import io.github.shared.local.data.gameobject.Cell;
import io.github.shared.local.data.gameobject.Shape;

public enum ShapeType {
    test(new Shape(new Cell[][]{
        {new Cell(ROAD),new Cell(ROAD),new Cell(ROAD)},
        {new Cell(ROAD),new Cell(ROAD),new Cell(VOID)},
        {new Cell(ROAD),new Cell(ROAD),new Cell(ROAD)},
    }));
    //MAP1, MAP2,
    //Barrack, Factory, Garage;

    private final Shape shape;

    ShapeType(Shape shape) {
        this.shape = shape;
    }

    public Shape getShape() {
        return shape;
    }
}
