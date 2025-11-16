package io.github.shared.local.data.EnumsTypes;

import java.util.HashMap;

public enum EntityType {
    test(Type.Building,WeaponType.test,1,1,1,1,1,1000,new HashMap<>()),

    BASE(
        Type.Building,
        ShapeType.Base,
        null,
        2000f,
        2,
        0f,
        5f,
        20,
        new HashMap<RessourcesType, Integer>() {{
            put(RessourcesType.Oil, 100);
            put(RessourcesType.Steel, 500);
        }},
        DeckCardCategory.Industrial
    ),
    BARRACK(
        Type.Building,
        ShapeType.Barrack,
        null,
        1500f,
        3,
        0f,
        4f,
        15,
        new HashMap<RessourcesType, Integer>() {{
            put(RessourcesType.Oil, 50);
            put(RessourcesType.Steel, 300);
        }},
        DeckCardCategory.Military
    ),
    FACTORY(
        Type.Building,
        ShapeType.Factory,
        null,
        2500f,
        2,
        0f,
        6f,
        25,
        new HashMap<RessourcesType, Integer>() {{
            put(RessourcesType.Oil, 200);
            put(RessourcesType.Steel, 700);
        }},
        DeckCardCategory.Military
    ),
    GARAGE(
        Type.Building,
        ShapeType.Garage,
        null,
        1800f,
        2,
        0f,
        5f,
        18,
        new HashMap<RessourcesType, Integer>() {{
            put(RessourcesType.Oil, 120);
            put(RessourcesType.Steel, 400);
        }},
        DeckCardCategory.Military
    );


    //INFANTRY(Type.Unit), HEAVE_INFANTRY(Type.Unit), TANK(Type.Unit), MOTORIZED(Type.Unit), WALKER(Type.Unit), AIRCRAFT(Type.Unit), NAVAL(Type.Unit);
    private final Type type;
    private final DeckCardCategory category;
    private final ShapeType shapeType;
    private final WeaponType weaponType;
    private final float maxHealth;
    private final int armor;
    private final float passiveHeal;
    private final float base_speed;
    private final float freeze_time;
    private final long create_time;
    private final HashMap<RessourcesType, Integer> cost;

    EntityType(Type type, ShapeType shapeType, WeaponType weaponType, float maxHealth, int armor, float passiveHeal, float freezeTime, long createTime, HashMap<RessourcesType, Integer> cost, DeckCardCategory category) {
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
        this.category = category;
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
        this.category = null;
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

    public DeckCardCategory getCategory(){
        return category;
    }

    public enum Type{
        Building,Unit
    }



}
