package io.github.shared.local.data.component;

import io.github.shared.local.data.gameobject.Weapon;

public class WeaponComponent {
    private Weapon data;         // Les stats de l'arme (damage, cooldown, range, etc.)
    private float currentCooldown; // Temps restant avant de pouvoir attaquer à nouveau

    public WeaponComponent(Weapon data) {
        this.data = data;
        this.currentCooldown = 0f;
    }

    /** @return true si l'arme peut attaquer */
    public boolean isReady() {
        return currentCooldown <= 0f;
    }

    /** Applique le cooldown après une attaque */
    public void use() {
        if (data != null) {
            currentCooldown = data.getCooldown();
        }
    }

    /** Met à jour le cooldown (appelé chaque frame) */
    public void update(float deltaTime) {
        if (currentCooldown > 0f) {
            currentCooldown -= deltaTime;
        }
    }

    /** Permet de changer d'arme à la volée */
    public void setWeapon(Weapon weapon) {
        this.data = weapon;
        this.currentCooldown = 0f; // Reset cooldown sur changement
    }

    public Weapon getWeapon() {
        return data;
    }

    public float getCurrentCooldown() {
        return currentCooldown;
    }
}
