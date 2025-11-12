package io.github.server.game_engine;

import com.artemis.WorldConfiguration;
import com.artemis.WorldConfigurationBuilder;

import io.github.server.data.ServerGame;

public class EcsServerEngine {
    public static WorldConfiguration serverWorldConfiguration(ServerGame game){
        return new WorldConfigurationBuilder()
            .build();
    }
}
