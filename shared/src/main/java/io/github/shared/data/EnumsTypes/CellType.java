package io.github.shared.data.enumsTypes;

import java.util.HashMap;

public enum CellType {
    VOID(0f,0f,0f,new HashMap<>(),false),
    GRASS(1f,1f,0f,new HashMap<>(),false),
    ROAD(1f,1f,0f,new HashMap<>(),false),
    WATER(0f,0f,1f,new HashMap<>(),false);
    private final float walkable;
    private final float flyable;
    private final float swimmable;
    public final HashMap<EntityType, Float> cellStats;
    private final boolean breakable;

    CellType(float walkable, float flyable, float swimmable, HashMap<EntityType, Float> cellStats, boolean breakable) {
        this.walkable = walkable;
        this.flyable = flyable;
        this.swimmable = swimmable;
        this.cellStats = cellStats;
        this.breakable = breakable;
    }

    public float getWalkable(EntityType entityType) {
        Float res = cellStats.get(entityType);
        if(res != null)return res;
        return walkable;
    }

    public float getFlyable(EntityType entityType) {
        Float res = cellStats.get(entityType);
        if(res != null)return res;
        return flyable;
    }

    public float getSwimmable(EntityType entityType){
        Float res = cellStats.get(entityType);
        if(res != null)return res;
        return swimmable;
    }

    public boolean isTraversable(EntityType entityType) {
        return (getWalkable(entityType) > 0) || (getFlyable(entityType) > 0) || (getSwimmable(entityType) > 0);
    }

    public float getMovementCost(EntityType entityType){
        return Math.max(getWalkable(entityType),Math.max(getFlyable(entityType),getSwimmable(entityType)));
    }

    public boolean isBreakable() {
        return breakable;
    }
}
