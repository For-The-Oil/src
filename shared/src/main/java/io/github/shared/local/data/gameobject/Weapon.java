package io.github.shared.local.data.gameobject;

/**
 * Classe servant à la représentation des armes.
 */
public class Weapon {
    private final float damage, range, penetration, cooldown;
    private final boolean aoe;

    /**
     * Une arme est constitué de plusieurs attributs :
     * damage =
     * @param damage
     * @param range
     * @param cooldown
     * @param penetration
     */
    public Weapon(float damage, float range, float cooldown, float penetration, boolean aoe) {
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

    public float getPenetration() {
        return penetration;
    }

    public float getRange() {
        return range;
    }

}
