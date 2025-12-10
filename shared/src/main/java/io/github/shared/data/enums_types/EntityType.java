package io.github.shared.data.enums_types;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

public enum EntityType {
    test(
        Type.Building,
        null,
        DeckCardCategory.Industrial,
        ShapeType.Base,
        null,
        2000f,
        2,
        0f,
        5f,
        20,
        new HashMap<ResourcesType, Integer>() {{
            put(ResourcesType.Oil, 100);
            put(ResourcesType.Steel, 500);
        }},
        new HashMap<ResourcesType, Integer>() {{
            put(ResourcesType.Oil, 100);
            put(ResourcesType.Steel, 500);
        }}
    ),


    // -------
    // Buildings
    // -------
    BASE(
        Type.Building,
        null,
        DeckCardCategory.Industrial,
        ShapeType.Base,
        null,
        2000f,
        2,
        0f,
        5f,
        20,
        new HashMap<ResourcesType, Integer>() {{
            put(ResourcesType.Oil, 100);
            put(ResourcesType.Steel, 500);
        }},
        null
    ),
    BARRACK(
        Type.Building,
        null,
        DeckCardCategory.Military,
        ShapeType.Barrack,
        null,
        1500f,
        3,
        0f,
        4f,
        15,
        new HashMap<ResourcesType, Integer>() {{
            put(ResourcesType.Oil, 50);
            put(ResourcesType.Steel, 300);
        }},
        null
    ),
    FACTORY(
        Type.Building,
        null,
        DeckCardCategory.Military,
        ShapeType.Factory,
        null,
        2500f,
        2,
        0f,
        6f,
        25,
        new HashMap<ResourcesType, Integer>() {{
            put(ResourcesType.Oil, 200);
            put(ResourcesType.Steel, 700);
        }},
        null
    ),
    GARAGE(
        Type.Building,
        null,
        DeckCardCategory.Military,
        ShapeType.Garage,
        null,
        1800f,
        2,
        0f,
        5f,
        18,
        new HashMap<ResourcesType, Integer>() {{
            put(ResourcesType.Oil, 120);
            put(ResourcesType.Steel, 400);
        }},
        null
    ),


    MINE(
        Type.Building,
        null,
        DeckCardCategory.Industrial,
        ShapeType.MINE,
        null, 300f,
        1,
        0f,
        3f,
        18,
        new HashMap<ResourcesType, Integer>() {{
            put(ResourcesType.Oil, 60);
            put(ResourcesType.Steel, 100);
        }},
        new HashMap<ResourcesType, Integer>() {{
            put(ResourcesType.Steel, 10);
        }}
    ),


    DERRICK(
        Type.Building,
        null,
        DeckCardCategory.Industrial,
        ShapeType.DERRICK,
        null,
        300f,
        1,
        0f,
        3f,
        18,
        new HashMap<ResourcesType, Integer>() {{
            put(ResourcesType.Oil, 60);
            put(ResourcesType.Steel, 100);
        }},
        new HashMap<ResourcesType, Integer>() {{
            put(ResourcesType.Oil, 10);
        }}
    ),




    // -------
    // Units
    // -------

    JEEP(
        Type.Unit, //Type type,
        GARAGE, //EntityType spawnFrom,
        new ArrayList<>(Collections.singleton(WeaponType.test)),  //WeaponType weaponType,
        200f, //float maxHealth,
        1, //int armor,
        0f,  //float passiveHeal,
        15f, //float baseSpeed,
        3f, //float freezeTime,
        5f, //float createTime,
        new HashMap<ResourcesType, Integer>() {{
            put(ResourcesType.Oil, 20);
            put(ResourcesType.Steel, 40);
        }} //HashMap<ResourcesType, Integer> cost
    ),

    BIKE(
        Type.Unit, //Type type,
        GARAGE, //EntityType spawnFrom,
        new ArrayList<>(Collections.singleton(WeaponType.test)),  //WeaponType weaponType,
        75f, //float maxHealth,
            0, //int armor,
            0f,  //float passiveHeal,
            40f, //float baseSpeed,
            1f, //float freezeTime,
            5f, //float createTime,
            new HashMap<ResourcesType, Integer>() {{
        put(ResourcesType.Oil, 10);
        put(ResourcesType.Steel, 20);
    }} //HashMap<ResourcesType, Integer> cost
    ),

    TANK(
        Type.Unit, //Type type,
        FACTORY, //EntityType spawnFrom,
        new ArrayList<>(Collections.singleton(WeaponType.test)),  //WeaponType weaponType,
        450f, //float maxHealth,
            2, //int armor,
            0f,  //float passiveHeal,
            10f, //float baseSpeed,
            5f, //float freezeTime,
            5f, //float createTime,
            new HashMap<ResourcesType, Integer>() {{
        put(ResourcesType.Oil, 100);
        put(ResourcesType.Steel, 300);
    }} //HashMap<ResourcesType, Integer> cost
    );






    private final Type type;
    private final DeckCardCategory category;
    private final EntityType spawnFrom;
    private final ShapeType shapeType;
    private final ArrayList<Float> projectileData;
    private final ArrayList<WeaponType> weaponType;
    private final float maxHealth;
    private final int armor;
    private final float passiveHeal;
    private final float base_speed;
    private final float freeze_time;
    private final float create_time;
    private final HashMap<ResourcesType, Integer> cost;
    private final HashMap<ResourcesType, Integer> production;

    EntityType(Type type, EntityType spawnFrom, DeckCardCategory category, ShapeType shapeType, ArrayList<WeaponType> weaponType, float maxHealth, int armor, float passiveHeal, float freezeTime, float createTime, HashMap<ResourcesType, Integer> cost, HashMap<ResourcesType, Integer> production) {
        this.type = type;
        this.category = category;
        this.spawnFrom = spawnFrom;
        this.shapeType = shapeType;
        this.weaponType = weaponType;
        this.production = production;
        this.projectileData = new ArrayList<>();
        this.maxHealth = maxHealth;
        this.armor = armor;
        this.passiveHeal = passiveHeal;
        this.freeze_time = freezeTime;
        this.create_time = createTime;
        this.cost = cost;
        this.base_speed = 0;
    }

    EntityType(Type type, EntityType spawnFrom, ArrayList<WeaponType> weaponType, float maxHealth, int armor, float passiveHeal, float baseSpeed, float freezeTime, float createTime, HashMap<ResourcesType, Integer> cost) {
        this.type = type;
        this.category = null;
        this.spawnFrom = spawnFrom;
        this.shapeType = null;
        this.projectileData = new ArrayList<>();
        this.weaponType = weaponType;
        this.production = null;
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
        this.production = null;
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

    public ArrayList<WeaponType> getWeaponType() {
        return weaponType;
    }

    public HashMap<ResourcesType, Integer> getCost() {
        return cost;
    }

    public float getCreate_time() {
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

    public Float getSpeed() {
        if(type.equals(Type.Projectile))return projectileData.get(4);
        return 0f;
    }

    public DeckCardCategory getCategory() {
        return category;
    }

    public HashMap<ResourcesType, Integer> getProduction() {
        return production;
    }


    public enum Type{
        Building,Unit,Projectile
    }
}
