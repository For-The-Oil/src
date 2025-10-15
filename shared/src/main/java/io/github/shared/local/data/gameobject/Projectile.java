package io.github.shared.local.data.gameobject;

import io.github.shared.local.data.component.WeaponComponent;
import io.github.shared.local.data.component.PositionComponent;

/**
 * Projectile volant au-dessus d'une map 2D (effet 2.5D)
 */
public class Projectile {
    private final WeaponComponent weapon;       // Dégâts et zone d'effet
    private final PositionComponent P;   // Position 2D
    private final float targetX, targetY;       // Coordonnées sur la map 2D
    private final float maxHeight;              // Hauteur maximale du missile (effet visuel)
    private final float speed;                  // Vitesse de déplacement
    private boolean active;
    private float height;                       // hauteur Z temporaire pour rendu

    public Projectile(WeaponComponent weapon, PositionComponent startPos, float targetX, float targetY, float maxHeight, float speed) {
        this.weapon = weapon;
        this.P = startPos;
        this.targetX = targetX;
        this.targetY = targetY;
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

    public void update(float deltaTime) {
        if (!active) return;

        float dx = targetX - P.position.x;
        float dy = targetY - P.position.y;
        float distance2D = (float) Math.sqrt(dx*dx + dy*dy);

        float move = speed * deltaTime;
        if (move >= distance2D) {
            P.position.x = targetX;
            P.position.y = targetY;
            explode();
        } else {
            P.position.x += dx / distance2D * move;
            P.position.y += dy / distance2D * move;
        }

        // Hauteur en arc de parabole pour effet visuel
        float t = distance2D / (distance2D + move); // approximation
        height = maxHeight * 4 * t * (1 - t);
    }

    private void explode() {
        active = false;
        // infliger dégâts avec weapon.data.damage
        // déclencher effets visuels/son
    }
}
