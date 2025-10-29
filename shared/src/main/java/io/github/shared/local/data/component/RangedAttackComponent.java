package io.github.shared.local.data.component;

import com.artemis.Component;

import io.github.shared.local.data.EnumsTypes.WeaponType;

public class RangedAttackComponent extends Component {
    public WeaponType weaponType;
    public int damage;
    public float cooldown;
    public float currentCooldown = 0f;
    public float range;
}
