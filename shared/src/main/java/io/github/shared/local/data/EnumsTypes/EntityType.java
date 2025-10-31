package io.github.shared.local.data.EnumsTypes;

import io.github.shared.local.data.gameobject.Shape;

public enum EntityType {
    test(Type.Building,1,1,1,1,1);
    //Barrack(Type.Building), Factory(Type.Building), Garage(Type.Building),
    //INFANTRY(Type.Unit), HEAVE_INFANTRY(Type.Unit), TANK(Type.Unit), MOTORIZED(Type.Unit), WALKER(Type.Unit), AIRCRAFT(Type.Unit), NAVAL(Type.Unit);
    private final Type type;
    private final ShapeType shapeType;
    private final float maxHealth;
    private final int armor;
    private final float passiveHeal;
    private final float base_speed;
    private final float freeze_time;

    EntityType(Type type, ShapeType shapeType, float maxHealth, int armor, float passiveHeal, float freezeTime) {
        this.type = type;
        this.shapeType = shapeType;
        this.maxHealth = maxHealth;
        this.armor = armor;
        this.passiveHeal = passiveHeal;
        this.freeze_time = freezeTime;
        this.base_speed = 0;
    }

    EntityType(Type type, float maxHealth, int armor, float passiveHeal, float baseSpeed, float freezeTime) {
        this.type = type;
        this.maxHealth = maxHealth;
        this.armor = armor;
        this.passiveHeal = passiveHeal;
        this.base_speed = baseSpeed;
        this.freeze_time = freezeTime;
        this.shapeType = null;
    }

    public Type getType(){
        return type;
    }
    public ShapeType getShapeType(){
        return shapeType;
    }

    public float getMaxHealth() {
        return maxHealth;
    }

    public int getArmor() {
        return armor;
    }

    public float getPassiveHeal() {
        return passiveHeal;
    }

    public float getBase_speed() {
        return base_speed;
    }

    public float getFreeze_time() {
        return freeze_time;
    }

    public enum Type{
        Building,Unit
    }
}
