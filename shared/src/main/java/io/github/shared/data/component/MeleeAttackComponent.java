package io.github.shared.data.component;

import io.github.shared.data.enumsTypes.WeaponType;

import com.artemis.PooledComponent;
import com.artemis.annotations.PooledWeaver;


@PooledWeaver
public class MeleeAttackComponent extends PooledComponent {
    public WeaponType weaponType;
    public int damage;
    public float cooldown;
    public float currentCooldown;
    public float reach;

    @Override
    public void reset() {
        weaponType = null;
        damage = 0;
        cooldown = 0f;
        currentCooldown = 0f;
        reach = 0f;
    }

    public void set(WeaponType weaponType, int damage, float cooldown, float reach) {
        this.weaponType = weaponType;
        this.damage = damage;
        this.cooldown = cooldown;
        this.reach = reach;
        this.currentCooldown = 0f;
    }

    public boolean isReady() {
        return currentCooldown <= 0f;
    }

    public void updateCooldown(float delta) {
        currentCooldown = Math.max(currentCooldown - delta, 0f);
    }
}

