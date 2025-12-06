package io.github.core.data.component;


import com.artemis.PooledComponent;

import java.util.HashMap;

import io.github.core.data.ExtendedModelInstance;
import io.github.core.data.enumsTypes.ModelType;

public class ModelComponent extends PooledComponent {
    public ExtendedModelInstance mapInstance;

    public ModelComponent() {
    }
    public ModelComponent(ExtendedModelInstance instance) {
        this.mapInstance = instance;
    }

    @Override
    public void reset() {
        mapInstance = null;
    }

}

