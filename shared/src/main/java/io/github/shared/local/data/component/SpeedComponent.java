package io.github.shared.local.data.component;

import com.artemis.Component;

import com.artemis.annotations.PooledWeaver;

import io.github.shared.local.data.EnumsTypes.EntityType;
import io.github.shared.local.data.EnumsTypes.WeaponType;


@PooledWeaver
public class SpeedComponent extends Component {
    public float base_speed;

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

