package io.github.shared.data.EnumsTypes;

import java.util.ArrayList;
import java.util.HashMap;

public enum EntityType {
    test(
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



    private final Type type;
    private final EntityType spawnFrom;
    private final DeckCardCategory category;
    private final ShapeType shapeType;
    private final ArrayList<Float> projectileData;
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
        this.category = category;
    }

    EntityType(Type type, EntityType spawnFrom, WeaponType weaponType, float maxHealth, int armor, float passiveHeal, float baseSpeed, float freezeTime, long createTime, HashMap<RessourcesType, Integer> cost) {
        this.type = type;
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
        this.category = null;
    }

    EntityType(Type type, EntityType spawnFrom,float damage, int armorPenetration, float aoe, float maxHeight, float baseSpeed, HashMap<RessourcesType, Integer> cost) {
        this.type = type;
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


    public DeckCardCategory getCategory(){
        return category;
    }

    public enum Type{
        Building,Unit,Projectile
    }
}
