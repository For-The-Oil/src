package io.github.shared.data.EnumsTypes;

public enum MapName {
    BASIC(ShapeType.test),
    BEACH(ShapeType.test),
    VOLCAN(ShapeType.test),
    GLACIER(ShapeType.test),
    TRAINING_GROUND(ShapeType.test),
    WASTELAND(ShapeType.test),
    BETA_TEST(ShapeType.test);

    private final ShapeType shapeType;
    MapName(ShapeType shapeType) {
        this.shapeType = shapeType;
    }

    public ShapeType getShapeType() {
        return shapeType;
    }
}
