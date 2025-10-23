package io.github.shared.local.ecs.component;


import com.artemis.Component;

import com.artemis.annotations.PooledWeaver;

@PooledWeaver
public class BalisticComponent extends Component {
    public float maxHeight;
    public boolean active;

}
