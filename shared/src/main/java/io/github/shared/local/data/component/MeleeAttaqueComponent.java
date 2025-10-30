package io.github.shared.local.data.component;

import com.artemis.Component;

import io.github.shared.local.data.EnumsTypes.WeaponType;

import com.artemis.annotations.PooledWeaver;

@PooledWeaver
public class MeleeAttaqueComponent extends Component {
    public WeaponType weaponType;
    public int damage;
    public float cooldown;
    public float currentCooldown = 0f;
    public float reach;

    public MeleeAttaqueComponent(WeaponType weaponType) {
        this.weaponType = weaponType;
        this.damage = weaponType.getDamage();
        this.cooldown = weaponType.getCooldown();
        this.currentCooldown = 0f;
        this.reach = weaponType.getReach();
    }
}
