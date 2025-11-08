package io.github.server.game_engine;

import com.artemis.WorldConfiguration;
import com.artemis.WorldConfigurationBuilder;

public class EcsServerEngine {
    public static WorldConfiguration serverWorldConfiguration(){
        return new WorldConfigurationBuilder()
            .build();
    }
}
