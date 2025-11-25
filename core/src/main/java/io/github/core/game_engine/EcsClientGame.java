package io.github.core.game_engine;

import com.artemis.WorldConfiguration;
import com.artemis.WorldConfigurationBuilder;

import io.github.shared.shared_engine.manager.DamageSystem;
import io.github.shared.shared_engine.system.FreezeSystem;
import io.github.shared.shared_engine.system.OnCreationSystem;
import io.github.shared.shared_engine.system.VectorApplicationSystem;

public class EcsClientGame {
    public static WorldConfiguration serverWorldConfiguration(ClientGame game){
        return new WorldConfigurationBuilder()
            .with(new FreezeSystem())
            .with(new OnCreationSystem())
            .with(new DamageSystem())
            .with(new VectorApplicationSystem())
            .build();
    }
}
