package io.github.shared.data.component;

import com.artemis.Component;
import com.badlogic.gdx.math.Vector2;

/**
 * Composant représentant la position d'une entité dans le monde (2D).
 */

import com.artemis.annotations.PooledWeaver;


/**
 * Composant représentant la position d'une entité dans le monde (2D).
 */
@PooledWeaver
public class PositionComponent extends Component {
    public float x;
    public float y;
    public float z;
    public float horizontalRotation;
    public float verticalRotation;

    public void reset() {
        x = y = z = 0f;
        horizontalRotation = verticalRotation = 0f;
    }

    public void set(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public void setRotation(float horizontal, float vertical) {
        this.horizontalRotation = horizontal;
        this.verticalRotation = vertical;
    }

    public void translate(float dx, float dy, float dz) {
        this.x += dx;
        this.y += dy;
        this.z += dz;
    }
}

