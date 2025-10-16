package io.github.shared.local.ecs.component;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.math.Vector3;

public class Position3DComponent implements Component {
    public final Vector3 position = new Vector3();
}
