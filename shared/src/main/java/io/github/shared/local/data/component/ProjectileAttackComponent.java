package io.github.shared.local.data.component;

import com.artemis.Component;
import com.artemis.annotations.PooledWeaver;

import io.github.shared.local.data.EnumsTypes.ProjectileType;
import io.github.shared.local.data.EnumsTypes.WeaponType;

@PooledWeaver
public class ProjectileAttackComponent extends Component {
    public WeaponType weaponType;
    public float cooldown;
    public float currentCooldown = 0f;
    public float range;
    public ProjectileType projectileType; // le type de projectile À envoyer

    public ProjectileAttackComponent(WeaponType weaponType,ProjectileType projectileType) {
        this.weaponType = weaponType;
        this.cooldown = weaponType.getCooldown();
        this.currentCooldown = 0f;
        this.range = weaponType.getReach();
        this.projectileType = projectileType;
    }
}
