package io.github.shared.local.data.component;

import com.artemis.Component;
import com.artemis.annotations.PooledWeaver;

import io.github.shared.local.data.EnumsTypes.ProjectileType;
import io.github.shared.local.data.EnumsTypes.WeaponType;


@PooledWeaver
public class ProjectileAttackComponent extends Component {
    public WeaponType weaponType;
    public float cooldown;
    public float currentCooldown;
    public float range;
    public ProjectileType projectileType;

    public void reset() {
        weaponType = null;
        cooldown = 0f;
        currentCooldown = 0f;
        range = 0f;
        projectileType = null;
    }

    public void set(WeaponType weaponType, float cooldown, float range, ProjectileType projectileType) {
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

