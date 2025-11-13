package io.github.shared.local.data.EnumsTypes;

import java.util.HashMap;

public enum EntityType {
    test(Type.Building,WeaponType.test,1,1,1,1,1,1000,new HashMap<>());
    //Barrack(Type.Building), Factory(Type.Building), Garage(Type.Building),
    //INFANTRY(Type.Unit), HEAVE_INFANTRY(Type.Unit), TANK(Type.Unit), MOTORIZED(Type.Unit), WALKER(Type.Unit), AIRCRAFT(Type.Unit), NAVAL(Type.Unit);
    private final Type type;
    private final ShapeType shapeType;
    private final WeaponType weaponType;
    private final float maxHealth;
    private final int armor;
    private final float passiveHeal;
    private final float base_speed;
    private final float freeze_time;
    private final long create_time;
    private final HashMap<RessourcesType, Integer> cost;

    EntityType(Type type, ShapeType shapeType, WeaponType weaponType, float maxHealth, int armor, float passiveHeal, float freezeTime, long createTime, HashMap<RessourcesType, Integer> cost) {
        this.type = type;
        this.shapeType = shapeType;
        this.weaponType = weaponType;
        this.maxHealth = maxHealth;
        this.armor = armor;
        this.passiveHeal = passiveHeal;
        this.freeze_time = freezeTime;
        this.create_time = createTime;
        this.cost = cost;
        this.base_speed = 0;
    }

    EntityType(Type type, WeaponType weaponType, float maxHealth, int armor, float passiveHeal, float baseSpeed, float freezeTime, long createTime, HashMap<RessourcesType, Integer> cost) {
        this.type = type;
        this.weaponType = weaponType;
        this.maxHealth = maxHealth;
        this.armor = armor;
        this.passiveHeal = passiveHeal;
        this.base_speed = baseSpeed;
        this.freeze_time = freezeTime;
        this.create_time = createTime;
        this.cost = cost;
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

    public WeaponType getWeaponType() {
        return weaponType;
    }

    public HashMap<RessourcesType, Integer> getCost() {
        return cost;
    }

    public long getCreate_time() {
        return create_time;
    }

    public enum Type{
        Building,Unit
    }
}
