package io.github.shared.local.data.component;

import io.github.shared.local.data.gameobject.Weapon;

public class WeaponComponent {
    private Weapon data;
    private float currentCooldown;

    public WeaponComponent(Weapon data) {
        this.data = data;
        this.currentCooldown = 0f;
    }

    public boolean isReady() {
        return currentCooldown <= 0f;
    }

    public void use() {
        currentCooldown = data.getCooldown();
    }

    public void setWeapon(Weapon weapon) {
        this.data = weapon;
        this.currentCooldown = 0f;
    }

    public Weapon getWeapon() {
        return data;
    }

    public float getCurrentCooldown() {
        return currentCooldown;
    }
}
