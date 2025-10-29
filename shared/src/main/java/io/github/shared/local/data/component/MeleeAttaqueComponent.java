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
}
