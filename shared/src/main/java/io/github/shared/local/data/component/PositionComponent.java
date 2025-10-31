package io.github.shared.local.data.component;

import com.artemis.Component;
import com.badlogic.gdx.math.Vector2;

/**
 * Composant représentant la position d'une entité dans le monde (2D).
 */

import com.artemis.annotations.PooledWeaver;

@PooledWeaver
public class PositionComponent extends Component {
    public float x;
    public float y;
    public float Z;
    public float horizontalRotation;
    public float verticalRotation;
}
