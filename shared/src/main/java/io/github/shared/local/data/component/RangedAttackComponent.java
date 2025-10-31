package io.github.shared.local.data.component;

import com.artemis.Component;
import com.artemis.annotations.PooledWeaver;

import io.github.shared.local.data.EnumsTypes.WeaponType;

@PooledWeaver
public class RangedAttackComponent extends Component {
    public WeaponType weaponType;
    public int damage;
    public float cooldown;
    public float currentCooldown;
    public float range;
}
