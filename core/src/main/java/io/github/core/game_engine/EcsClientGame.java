package io.github.core.game_engine;

import com.artemis.WorldConfiguration;
import com.artemis.WorldConfigurationBuilder;

import io.github.core.data.ClientGame;
import io.github.core.game_engine.system.GraphicsSyncSystem;
import io.github.shared.shared_engine.system.DamageSystem;
import io.github.shared.shared_engine.system.FreezeSystem;
import io.github.shared.shared_engine.system.MovementSystem;
import io.github.shared.shared_engine.system.OnCreationSystem;
import io.github.shared.shared_engine.system.VectorApplicationSystem;

public class EcsClientGame {
    public static WorldConfiguration serverWorldConfiguration(ClientGame game){
        return new WorldConfigurationBuilder()
            .with(new OnCreationSystem())
            .with(new FreezeSystem())
            .with(new DamageSystem())
            .with(new MovementSystem())
            .with(new VectorApplicationSystem())
            .with(new GraphicsSyncSystem(game))
            .build();
    }
}
