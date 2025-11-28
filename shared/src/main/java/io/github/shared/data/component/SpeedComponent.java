package io.github.shared.data.component;

import com.artemis.Component;

import com.artemis.PooledComponent;
import com.artemis.annotations.PooledWeaver;


@PooledWeaver
public class SpeedComponent extends PooledComponent {
    public float base_speed;

    @Override
    public void reset() {
        base_speed = 0f;
    }

    public void set(float speed) {
        this.base_speed = speed;
    }

    public float getSpeed() {
        return base_speed;
    }
}

