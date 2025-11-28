package io.github.shared.data.component;

import com.artemis.Component;
import com.artemis.PooledComponent;
import com.artemis.annotations.PooledWeaver;

import io.github.shared.data.EnumsTypes.EntityType;
import io.github.shared.data.EnumsTypes.WeaponType;


@PooledWeaver
public class ProjectileAttackComponent extends PooledComponent {
    public WeaponType weaponType;
    public float cooldown;
    public float currentCooldown;
    public float range;
    public EntityType projectileType;

    @Override
    public void reset() {
        weaponType = null;
        cooldown = 0f;
        currentCooldown = 0f;
        range = 0f;
        projectileType = null;
    }

    public void set(WeaponType weaponType, float cooldown, float range, EntityType projectileType) {
        this.weaponType = weaponType;
        this.cooldown = cooldown;
        this.range = range;
        this.projectileType = projectileType;
        this.currentCooldown = 0f;
    }

    public boolean isReady() {
        return currentCooldown <= 0f;
    }

    public void updateCooldown(float delta) {
        currentCooldown = Math.max(currentCooldown - delta, 0f);
    }
}

