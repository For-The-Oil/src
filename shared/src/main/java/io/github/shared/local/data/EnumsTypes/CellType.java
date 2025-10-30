package io.github.shared.local.data.EnumsTypes;

import java.util.HashMap;

public enum CellType {
    GRASS(1f,1f,0f,new HashMap<>()),
    ROAD(1f,1f,0f,new HashMap<>()),
    WATER(0f,0f,1f,new HashMap<>());
    private final float walkable;
    private final float flyable;
    private final float swimmable;
    public final HashMap<EntityType, Float> cellStats;
    CellType(float walkable, float flyable, float swimmable, HashMap<EntityType, Float> cellStats) {
        this.walkable = walkable;
        this.flyable = flyable;
        this.swimmable = swimmable;
        this.cellStats = cellStats;
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

    public boolean isWalkable() {
        return this.walkable > 0;
    }

    public boolean isFlyable() {
        return this.flyable > 0;
    }

    public boolean isSwimmable(){
        return this.swimmable > 0;
    }
}
