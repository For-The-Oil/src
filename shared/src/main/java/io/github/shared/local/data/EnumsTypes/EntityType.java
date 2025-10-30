package io.github.shared.local.data.EnumsTypes;

import io.github.shared.local.data.gameobject.Shape;

public enum EntityType {
    test(Type.Building,1,1,1,1);
    //Barrack(Type.Building), Factory(Type.Building), Garage(Type.Building),
    //INFANTRY(Type.Unit), HEAVE_INFANTRY(Type.Unit), TANK(Type.Unit), MOTORIZED(Type.Unit), WALKER(Type.Unit), AIRCRAFT(Type.Unit), NAVAL(Type.Unit);
    private final Type type;
    private final Shape shape;
    private final float maxHealth;
    private final int armor;
    private final float passiveHeal;
    private final float base_speed;

    EntityType(Type type, Shape shape, float maxHealth, int armor, float passiveHeal) {
        this.type = type;
        this.shape = shape;
        this.maxHealth = maxHealth;
        this.armor = armor;
        this.passiveHeal = passiveHeal;
        this.base_speed = 0;
    }

    EntityType(Type type, float maxHealth, int armor, float passiveHeal, float baseSpeed) {
        this.type = type;
        this.maxHealth = maxHealth;
        this.armor = armor;
        this.passiveHeal = passiveHeal;
        this.base_speed = baseSpeed;
        this.shape = null;
    }

    public Type getType(){
        return type;
    }
    public Shape getShape(){
        return shape;
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

    public enum Type{
        Building,Unit
    }
}
