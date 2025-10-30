package io.github.shared.local.data.EnumsTypes;

public enum WeaponType {
    test(Type.Melee,1,1,1);
    //RIFLE(Type.Range), SHOTGUN(Type.Range), BAZOOKA(Type.Range),
    //SWORD(Type.Melee), SPEAR(Type.Melee), CHAINSAW(Type.Melee);
    private final Type type;
    private final int damage;
    private final float cooldown;
    private final float reach;

    WeaponType(Type type, int damage, float cooldown, float reach) {
        this.type = type;
        this.damage = damage;
        this.cooldown = cooldown;
        this.reach = reach;
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

    public enum Type{
        Range,Melee,ProjectileLauncher
    }
}
