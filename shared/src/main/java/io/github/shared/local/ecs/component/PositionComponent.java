package io.github.shared.local.ecs.component;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.math.Vector2;

/**
 * Composant représentant la position d'une entité dans le monde (2D).
 */
public class PositionComponent implements Component {
    public final Vector2 position = new Vector2();

    public PositionComponent() {
    }

    public PositionComponent(float x, float y) {
        this.position.set(x, y);
    }

}
