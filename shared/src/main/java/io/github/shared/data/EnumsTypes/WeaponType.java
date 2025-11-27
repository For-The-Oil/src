package io.github.shared.data.EnumsTypes;

public enum WeaponType {
    test(Type.Melee,1,1,1,1,1,false);
    //RIFLE(Type.Range), SHOTGUN(Type.Range), BAZOOKA(Type.Range),
    //SWORD(Type.Melee), SPEAR(Type.Melee), CHAINSAW(Type.Melee);
    private final Type type;
    private final int damage;
    private final int armorPenetration;
    private final float cooldown;
    private final float animationAndFocusCooldown;
    private final float reach;
    private final boolean HitAndMove;
    private final EntityType projectileType;

    WeaponType(Type type, int damage, int armorPenetration, float cooldown, float animationAndFocusCooldown, float reach, boolean hitAndMove) {
        this.type = type;
        this.damage = damage;
        this.armorPenetration = armorPenetration;
        this.cooldown = cooldown;
        this.animationAndFocusCooldown = animationAndFocusCooldown;
        this.reach = reach;
        HitAndMove = hitAndMove;
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

    public enum Type{
        Range,Melee,ProjectileLauncher
    }
}
