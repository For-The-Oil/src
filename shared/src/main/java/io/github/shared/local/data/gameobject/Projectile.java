package io.github.shared.local.data.gameobject;

import java.awt.geom.Point2D;

import io.github.shared.local.data.component.PositionComponent;

/**
 * Projectile volant au-dessus d'une map 2D (effet 2.5D)
 */
public class Projectile {
    private final PositionComponent P;
    private final Point2D point;
    private final float maxHeight;
    private final float speed;
    private boolean active;
    private float height;

    public Projectile(PositionComponent startPos, Point2D point, float maxHeight, float speed) {
        this.P = startPos;
        this.point = point;
        this.maxHeight = maxHeight;
        this.speed = speed;
        this.active = true;
        this.height = 0f; // départ depuis le sol
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
