package io.github.shared.data.enumsTypes;

public enum WeaponType {
    test(Type.Melee,1,1,1,1,1,(float) Math.toRadians(360f),1,1,1,1,false,true);
    //RIFLE(Type.Range), SHOTGUN(Type.Range), BAZOOKA(Type.Range),
    //SWORD(Type.Melee), SPEAR(Type.Melee), CHAINSAW(Type.Melee);
    private final Type type;
    private final int damage;
    private final int armorPenetration;
    private final float cooldown;
    private final float animationCooldown;
    private final float animationAndFocusCooldown;

    private final float turn_speed;
    private final float reach;
    private final float  translationX ;
    private final float  translationY ;
    private final float   translationZ ;
    private final boolean HitAndMove;
    private final boolean isTurret;
    private final EntityType projectileType;

    WeaponType(Type type, int damage, int armorPenetration, float cooldown, float animationCooldown, float animationAndFocusCooldown, float turnSpeed, float reach, float translationX, float translationY, float translationZ, boolean hitAndMove, boolean isTurret) {
        this.type = type;
        this.damage = damage;
        this.armorPenetration = armorPenetration;
        this.cooldown = cooldown;
        this.animationCooldown = animationCooldown;
        this.animationAndFocusCooldown = animationAndFocusCooldown;
        this.turn_speed = turnSpeed;
        this.reach = reach;
        this.translationX = translationX;
        this.translationY = translationY;
        this.translationZ = translationZ;
        this.HitAndMove = hitAndMove;
        this.isTurret = isTurret;
        this.projectileType = null;
    }

    public Type getType() {
        return type;
    }

    public int getDamage() {
        return damage;
    }

    public float getCooldown() {
        return cooldown;
    }

    public float getReach() {
        return reach;
    }

    public EntityType getProjectileType() {
        return projectileType;
    }

    public int getArmorPenetration() {
        return armorPenetration;
    }

    public float getAnimationAndFocusCooldown() {
        return animationAndFocusCooldown;
    }

    public boolean isHitAndMove() {
        return HitAndMove;
    }

    public float getAnimationCooldown() {
        return animationCooldown;
    }

    public float getTranslationX() {
        return translationX;
    }

    public float getTranslationY() {
        return translationY;
    }

    public float getTranslationZ() {
        return translationZ;
    }

    public boolean isTurret() {
        return isTurret;
    }

    public float getTurn_speed() {
        return turn_speed;
    }

    public enum Type{
        Range,Melee,ProjectileLauncher
    }
}
