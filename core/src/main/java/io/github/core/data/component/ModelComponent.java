package io.github.core.data.component;


import com.artemis.PooledComponent;

import java.util.HashMap;

import io.github.core.data.ExtendedModelInstance;
import io.github.core.data.enumsTypes.ModelType;

public class ModelComponent extends PooledComponent {
    public HashMap<ModelType, ExtendedModelInstance> mapInstance;

    public ModelComponent(ExtendedModelInstance instance) {
        this.mapInstance = new HashMap<>();
        mapInstance.put(ModelType.Entity,instance);
    }

    @Override
    public void reset() {
        mapInstance.clear();
    }

}

