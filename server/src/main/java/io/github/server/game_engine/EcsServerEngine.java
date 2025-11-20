package io.github.server.game_engine;

import com.artemis.WorldConfiguration;
import com.artemis.WorldConfigurationBuilder;

import io.github.server.data.ServerGame;
import io.github.server.game_engine.system.DamageSystem;
import io.github.server.game_engine.system.ProductionResourcesSystem;
import io.github.server.game_engine.system.VectorApplicationSystem;


/**
 * Builds the Artemis World configuration for the server.
 */
public class EcsServerEngine {

    public static WorldConfiguration serverWorldConfiguration(ServerGame game) {
        return new WorldConfigurationBuilder()
            // Register systems here:
            .with(new ProductionResourcesSystem(game))
            .with(new DamageSystem(game))
            .with(new VectorApplicationSystem())
            .build();
    }
}

