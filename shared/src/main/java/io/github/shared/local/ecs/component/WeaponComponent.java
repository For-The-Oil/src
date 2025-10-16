package io.github.shared.local.ecs.component;

import com.badlogic.ashley.core.Component;

import io.github.shared.local.data.gameobject.Weapon;

public class WeaponComponent implements Component {
    public Weapon data;
    public float currentCooldown;

    public WeaponComponent(Weapon data) {
        this.data = data;
        this.currentCooldown = 0f;
    }

}
