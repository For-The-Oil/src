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

    /** 0 rad = NORTH, π/2 rad = EAST, π rad = SOUTH, 3π/2 rad = WEST (sens horaire) */
    public float getAngleRadians() {
        switch (this) {
            case NORTH: return 0.0f;
            case EAST:  return (float) (Math.PI / 2);
            case SOUTH: return (float) Math.PI;
            case WEST:  return (float) (3 * Math.PI / 2);
            default: throw new IllegalArgumentException("Unknown direction: " + this);
        }
    }



}
