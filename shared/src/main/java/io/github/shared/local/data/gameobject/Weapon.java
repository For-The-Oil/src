package io.github.shared.local.data.gameobject;

/**
 * Classe servant à la représentation des armes.
 */
public class Weapon {
    private final float damage, range, cooldown;
    private final int penetration;
    private final boolean aoe;
    public Weapon(float damage, float range, float cooldown, int penetration, boolean aoe) {
        this.damage = damage;
        this.range =  range;
        this.cooldown = cooldown;
        this.penetration = penetration;
        this.aoe = aoe;
    }

    public float getCooldown() {
        return cooldown;
    }

    public boolean isAoe() {
        return aoe;
    }

    public float getDamage() {
        return damage;
    }

    public int getPenetration() {
        return penetration;
    }

    public float getRange() {
        return range;
    }

}
