package io.github.shared.local.ecs.component;

import com.artemis.Component;

import io.github.shared.local.data.gameobject.Weapon;

import com.artemis.annotations.PooledWeaver;

@PooledWeaver
public class WeaponComponent extends Component {
    public Weapon data;
    public float currentCooldown;

    public WeaponComponent(Weapon data) {
        this.data = data;
        this.currentCooldown = 0f;
    }

}
