package io.github.shared.local.data.gameobject;

import java.awt.geom.Point2D;

import io.github.shared.local.data.component.PositionComponent;

/**
 * Projectile volant au-dessus d'une map 2D (effet 2.5D)
 */
public class Projectile {
    private final PositionComponent position;

    public Projectile(PositionComponent startPos, Point2D point, float maxHeight, float speed) {
        this.position = startPos;
        this.point = point;
        this.maxHeight = maxHeight;
        this.speed = speed;
        this.active = true;
        this.height = 0f;
    }

    public boolean isActive() {
        return active;
    }

    public float getHeight() {
        return height;
    }

    public float getSpeed() {
        return speed;
    }

    public float getMaxHeight() {
        return maxHeight;
    }

    public Point2D getPoint() {
        return point;
    }
}
