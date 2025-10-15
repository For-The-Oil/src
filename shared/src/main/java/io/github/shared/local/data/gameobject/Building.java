package io.github.shared.local.data.gameobject;

import io.github.shared.local.data.component.ShapeComponent;
import io.github.shared.local.data.component.LifeComponent;

public class Building {
    private final String name;
    private final int mapX, mapY; // Position sur la map case en haut à gauche
    private final ShapeComponent shape; // La forme interne du bâtiment
    private final LifeComponent life;

    public Building(String name, int mapX, int mapY, ShapeComponent shape, LifeComponent life) {
        this.name = name;
        this.mapX = mapX;
        this.mapY = mapY;
        this.shape = shape;
        this.life = life;
    }

    public String getName() {
        return name;
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
}
