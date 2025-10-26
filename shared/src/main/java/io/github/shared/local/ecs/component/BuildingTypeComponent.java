package io.github.shared.local.ecs.component;


import com.artemis.Component;
import com.artemis.annotations.PooledWeaver;

import io.github.shared.local.data.EnumsTypes.BuildingType;

@PooledWeaver
public class BuildingTypeComponent extends Component {
    public BuildingType type;
}
