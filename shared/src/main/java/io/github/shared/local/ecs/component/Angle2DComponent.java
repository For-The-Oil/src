package io.github.shared.local.ecs.component;

import com.artemis.Component;

import com.artemis.annotations.PooledWeaver;

@PooledWeaver
public class Angle2DComponent extends Component {
    public float angle;
}
