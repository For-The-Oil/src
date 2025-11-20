package io.github.shared.data.EnumsTypes;

import java.util.ArrayList;
import java.util.HashMap;

public enum EntityType {
    test(Type.Building,null,WeaponType.test,1,1,1,1,1,1000,new HashMap<>());
    //Barrack(Type.Building), Factory(Type.Building), Garage(Type.Building),
    //INFANTRY(Type.Unit), HEAVE_INFANTRY(Type.Unit), TANK(Type.Unit), MOTORIZED(Type.Unit), WALKER(Type.Unit), AIRCRAFT(Type.Unit), NAVAL(Type.Unit);
    private final Type type;
    private final DeckCardCategory category;
    private final EntityType spawnFrom;
    private final ShapeType shapeType;
    private final ArrayList<Float> projectileData;
    private final WeaponType weaponType;
    private final float maxHealth;
    private final int armor;
    private final float passiveHeal;
    private final float base_speed;
    private final float freeze_time;
    private final long create_time;
    private final HashMap<ResourcesType, Integer> cost;

    EntityType(Type type, DeckCardCategory category, ShapeType shapeType, WeaponType weaponType, float maxHealth, int armor, float passiveHeal, float freezeTime, long createTime, HashMap<ResourcesType, Integer> cost) {
        this.type = type;
        this.category = category;
        this.spawnFrom = null;
        this.shapeType = shapeType;
        this.weaponType = weaponType;
        this.projectileData = new ArrayList<>();
        this.maxHealth = maxHealth;
        this.armor = armor;
        this.passiveHeal = passiveHeal;
        this.freeze_time = freezeTime;
        this.create_time = createTime;
        this.cost = cost;
        this.base_speed = 0;
    }

    EntityType(Type type, EntityType spawnFrom, WeaponType weaponType, float maxHealth, int armor, float passiveHeal, float baseSpeed, float freezeTime, long createTime, HashMap<ResourcesType, Integer> cost) {
        this.type = type;
        this.category = null;
        this.spawnFrom = spawnFrom;
        this.shapeType = null;
        this.projectileData = new ArrayList<>();
        this.weaponType = weaponType;
        this.maxHealth = maxHealth;
        this.armor = armor;
        this.passiveHeal = passiveHeal;
        this.base_speed = baseSpeed;
        this.freeze_time = freezeTime;
        this.create_time = createTime;
        this.cost = cost;
    }

    EntityType(Type type, EntityType spawnFrom,float damage, int armorPenetration, float aoe, float maxHeight, float baseSpeed, HashMap<ResourcesType, Integer> cost) {
        this.type = type;
        this.category = null;
        this.spawnFrom = spawnFrom;
        this.shapeType = null;
        this.projectileData = new ArrayList<>();
        this.projectileData.add(damage);
        this.projectileData.add((float) armorPenetration);
        this.projectileData.add(aoe);
        this.projectileData.add(maxHeight);
        this.weaponType = null;
        this.maxHealth = 0;
        this.armor = 0;
        this.passiveHeal = 0;
        this.base_speed = baseSpeed;
        this.freeze_time = 0;
        this.create_time = 0;
        this.cost = cost;
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

    public HashMap<ResourcesType, Integer> getCost() {
        return cost;
    }

    public long getCreate_time() {
        return create_time;
    }

    public EntityType getFrom() {
        return spawnFrom;
    }

    public Integer getDamage() {
        if(type.equals(Type.Projectile))return projectileData.get(0).intValue();
        return 0;
    }
    public Integer getArmorPenetration() {
        if(type.equals(Type.Projectile))return projectileData.get(1).intValue();
        return 0;
    }
    public Float getAoe() {
        if(type.equals(Type.Projectile))return projectileData.get(2);
        return 0f;
    }
    public Float getMaxHeight() {
        if(type.equals(Type.Projectile))return projectileData.get(3);
        return 0f;
    }

    public DeckCardCategory getCategory() {
        return category;
    }


    public enum Type{
        Building,Unit,Projectile
    }
}
