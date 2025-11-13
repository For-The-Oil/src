package io.github.core.game_engine;

import com.artemis.WorldConfiguration;
import com.artemis.WorldConfigurationBuilder;

public class EcsClientGame {
    public static WorldConfiguration serverWorldConfiguration(ClientGame game){
        return new WorldConfigurationBuilder()
            .build();
    }
}
