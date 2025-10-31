package io.github.shared.local.data.EnumsTypes;

public enum ProjectileType {
    test(1,1,1,1);
    //Missile, Nuke, AirStrike;
    private final int damage;
    private final float aoe;
    private final float maxHeight;
    private final float speed;

    ProjectileType(int damage, float aoe, float maxHeight, float speed) {
        this.damage = damage;
        this.aoe = aoe;
        this.maxHeight = maxHeight;
        this.speed = speed;
    }

    public int getDamage() {
        return damage;
    }

    public float getAoe() {
        return aoe;
    }

    public float getMaxHeight() {
        return maxHeight;
    }


    public float getSpeed() {
        return speed;
    }
}
