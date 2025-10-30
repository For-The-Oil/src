package io.github.shared.local.data.component;

import com.artemis.Component;

import com.artemis.annotations.PooledWeaver;

import io.github.shared.local.data.EnumsTypes.EntityType;
import io.github.shared.local.data.EnumsTypes.WeaponType;

@PooledWeaver
public class SpeedComponent extends Component {
    public float base_speed;
    public SpeedComponent(EntityType entityType) {
        this.base_speed = entityType.getBase_speed();
    }

}
