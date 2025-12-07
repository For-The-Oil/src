package io.github.shared.data.enums_types;

public enum Direction {
    NORTH, SOUTH, EAST, WEST;

    public Direction opposite() {
        switch (this) {
            case NORTH: return SOUTH;
            case SOUTH: return NORTH;
            case EAST: return WEST;
            case WEST: return EAST;
            default:throw new IllegalArgumentException("Unknown direction: "+this);
        }
    }

    public Direction rotateClockwise() {
        switch (this) {
            case NORTH: return EAST;
            case EAST: return SOUTH;
            case SOUTH: return WEST;
            case WEST: return NORTH;
            default:throw new IllegalArgumentException("Unknown direction: "+this);
        }
    }


    /** 0° = NORTH, 90° = EAST, 180° = SOUTH, 270° = WEST (sens horaire) */
    public int getAngleDegrees() {
        switch (this) {
            case NORTH: return 0;
            case EAST:  return 90;
            case SOUTH: return 180;
            case WEST:  return 270;
            default: throw new IllegalArgumentException("Unknown direction: " + this);
        }
    }

}
