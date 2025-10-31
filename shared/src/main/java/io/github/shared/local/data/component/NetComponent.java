package io.github.shared.local.data.component;

import com.artemis.Component;
import com.artemis.annotations.PooledWeaver;

import io.github.shared.local.data.EnumsTypes.EntityType;


@PooledWeaver
public class NetComponent extends Component {
    public int netId;
    public EntityType entityType;

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

