package io.github.shared.data.component;

import com.artemis.PooledComponent;
import com.artemis.annotations.PooledWeaver;

import io.github.shared.data.enums_types.WeaponType;


@PooledWeaver
public class RangedAttackComponent extends PooledComponent {
    public WeaponType weaponType;
    public int damage;
    public float cooldown;
    public float currentCooldown;
    public float range;
    public float horizontalRotation;
    public float verticalRotation;

    @Override
    public void reset() {
        weaponType = null;
        damage = 0;
        cooldown = 0f;
        currentCooldown = 0f;
        range = 0f;
        horizontalRotation = 0f;
        verticalRotation = 0f;
    }

    public void set(WeaponType weaponType, int damage, float cooldown, float range) {
        this.weaponType = weaponType;
        this.damage = damage;
        this.cooldown = cooldown;
        this.range = range;
        this.currentCooldown = 0f;
        this.horizontalRotation = 0f;
        this.verticalRotation = 0f;
    }

    public boolean isReady() {
        return currentCooldown <= 0f;
    }

    public void updateCooldown(float delta) {
        currentCooldown = Math.max(currentCooldown - delta, 0f);
    }
}

