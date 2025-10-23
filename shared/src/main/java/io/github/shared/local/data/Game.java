package io.github.shared.local.data;

import com.artemis.World;
import com.artemis.WorldConfigurationBuilder;
import com.artemis.Entity;

public class Game {

    private World world;
    private Entity WORLD_MAP;
    public Game(){

        World world = new World(new WorldConfigurationBuilder().build());

    }

}
