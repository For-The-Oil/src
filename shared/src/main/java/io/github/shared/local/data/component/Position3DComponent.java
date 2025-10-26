package io.github.shared.local.data.component;

import com.artemis.Component;
import com.badlogic.gdx.math.Vector3;

import com.artemis.annotations.PooledWeaver;

@PooledWeaver
public class Position3DComponent extends Component {
    public final Vector3 position = new Vector3();
}
