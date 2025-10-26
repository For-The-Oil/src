package io.github.shared.local.data.component;

import com.artemis.Component;

import io.github.shared.local.data.EnumsTypes.WeaponType;

import com.artemis.annotations.PooledWeaver;

@PooledWeaver
public class WeaponComponent extends Component {
    public WeaponType data;
    public float currentCooldown;

    public WeaponComponent(WeaponType data) {
        this.data = data;
        this.currentCooldown = 0f;
    }

}
