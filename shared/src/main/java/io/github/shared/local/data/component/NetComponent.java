package io.github.shared.local.data.component;

import com.artemis.Component;
import com.artemis.annotations.PooledWeaver;

import io.github.shared.local.data.EnumsTypes.EntityType;

@PooledWeaver
public class NetComponent extends Component {
    public int netId;
    public EntityType entityType;

}
