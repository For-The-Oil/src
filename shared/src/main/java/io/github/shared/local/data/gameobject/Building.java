package io.github.shared.local.data.gameobject;

import io.github.shared.local.data.component.ShapeComponent;
import io.github.shared.local.data.component.LifeComponent;
import io.github.shared.local.data.component.WeaponComponent;

public class Building {
    private final String idBuilding;
    private final int mapX, mapY;
    private final ShapeComponent shape;
    private final LifeComponent life;

    private final WeaponComponent weapon;

    public Building(String idBuilding, int mapX, int mapY, ShapeComponent shape, LifeComponent life, WeaponComponent weapon) {
        this.idBuilding = idBuilding;
        this.mapX = mapX;
        this.mapY = mapY;
        this.shape = shape;
        this.life = life;
        this.weapon = weapon;
    }

    public String getIdBuilding() {
        return idBuilding;
    }

    public int getMapX() {
        return mapX;
    }

    public int getMapY() {
        return mapY;
    }

    public ShapeComponent getShape() {
        return shape;
    }

    public LifeComponent getLife() {
        return life;
    }

    public WeaponComponent getWeapon() {
        return weapon;
    }
}
