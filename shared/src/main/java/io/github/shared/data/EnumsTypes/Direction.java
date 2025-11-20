package io.github.shared.data.EnumsTypes;

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
}
