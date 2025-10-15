package io.github.shared.local.data.gameobject;

import io.github.shared.local.data.component.LifeComponent;
import io.github.shared.local.data.component.PositionComponent;
import io.github.shared.local.data.component.WeaponComponent;

/**
 * Représente une unité du jeu.
 */
public class Unit {
    private final String idUnit;
    private final PositionComponent position;
    private final WeaponComponent weapon;
    private final LifeComponent life;

    public Unit(String idUnit, PositionComponent position, WeaponComponent weapon, LifeComponent life) {
        this.idUnit = idUnit;
        this.position = position;
        this.weapon = weapon;
        this.life = life;
    }

    public String getIdUnit() {
        return idUnit;
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

}
