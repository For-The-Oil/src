package io.github.shared.data.component;

import com.artemis.PooledComponent;
import com.artemis.annotations.PooledWeaver;

import io.github.shared.data.enums_types.EntityType;


@PooledWeaver
public class NetComponent extends PooledComponent {
    public int netId;
    public EntityType entityType;

    @Override
    public void reset() {
        netId = -1;
        entityType = null;
    }

    public void set(int netId, EntityType entityType) {
        this.netId = netId;
        this.entityType = entityType;
    }

    public boolean isValid() {
        return netId >= 0 && entityType != null;
    }
}

