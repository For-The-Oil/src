package io.github.shared.local.ecs.component;

import com.artemis.Component;
import com.badlogic.gdx.math.Vector2;

/**
 * Composant représentant la position d'une entité dans le monde (2D).
 */

import com.artemis.annotations.PooledWeaver;

@PooledWeaver
public class PositionComponent extends Component {
    public final Vector2 position = new Vector2();

    public PositionComponent() {
    }

    public PositionComponent(float x, float y) {
        this.position.set(x, y);
    }

}
