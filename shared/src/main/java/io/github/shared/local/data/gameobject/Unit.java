package io.github.shared.local.data.gameobject;

import io.github.shared.local.data.component.LifeComponent;
import io.github.shared.local.data.component.PositionComponent;
import io.github.shared.local.data.component.WeaponComponent;

/**
 * Représente une unité du jeu.
 */
public class Unit {
    private final String name;
    private final PositionComponent position;
    private final WeaponComponent weapon;
    private final LifeComponent life;

    public Unit(String name, PositionComponent position, WeaponComponent weapon, LifeComponent life) {
        this.name = name;
        this.position = position;
        this.weapon = weapon;
        this.life = life;
    }

    public String getName() {
        return name;
    }

    public PositionComponent getPosition() {
        return position;
    }

    public WeaponComponent getWeapon() {
        return weapon;
    }

    public LifeComponent getLife() {
        return life;
    }

    /**
     * Fait attaquer l'unité sur une cible.
     */
    public void attack(Unit target) {
        if (weapon != null && life != null && target != null) {
            if (weaponReady()) {
                target.getLife().takeDamage(weapon.getWeapon().getDamage());
                resetWeaponCooldown();
            }
        }
    }

    /**
     * Vérifie si le cooldown de l'arme est écoulé.
     */
    private boolean weaponReady() {
        // Ici on peut stocker un cooldown interne pour chaque unité
        // Par exemple un float timeSinceLastAttack
        return true; // Placeholder
    }

    /**
     * Reset le cooldown de l'arme après une attaque.
     */
    private void resetWeaponCooldown() {
        // Mettre à jour le cooldown interne ici
    }

    /**
     * Met à jour l'unité chaque frame (regénération, cooldown, etc.)
     */
    public void update(float deltaTime) {
        life.update(deltaTime);
        weapon.update(deltaTime);
    }
}
