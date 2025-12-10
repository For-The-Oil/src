package io.github.server.game_engine;

import com.artemis.WorldConfiguration;
import com.artemis.WorldConfigurationBuilder;

import io.github.server.data.ServerGame;
import io.github.server.game_engine.system.DamageServerSystem;
import io.github.server.game_engine.system.FreezeServerSystem;
import io.github.server.game_engine.system.HealSystem;
import io.github.server.game_engine.system.MeleeAttackSystem;
import io.github.server.game_engine.system.MovementServerSystem;
import io.github.server.game_engine.system.OnCreationServerSystem;
import io.github.server.game_engine.system.ProductionResourcesSystem;
import io.github.server.game_engine.system.ProjectileAttackSystem;
import io.github.server.game_engine.system.ProjectileImpactSystem;
import io.github.server.game_engine.system.ProjectileMovementSystem;
import io.github.server.game_engine.system.RangedAttackSystem;
import io.github.shared.shared_engine.system.MovementSystem;
import io.github.shared.shared_engine.system.VectorApplicationSystem;
import io.github.shared.shared_engine.system.DamageAndHealSystem;
import io.github.shared.shared_engine.system.FreezeSystem;
import io.github.shared.shared_engine.system.OnCreationSystem;


/**
 * Builds the Artemis World configuration for the server.
 */
public class EcsServerEngine {

    public static WorldConfiguration serverWorldConfiguration(ServerGame game) {
        return new WorldConfigurationBuilder()
            // Register systems here:
            .with(new ProductionResourcesSystem(game))
            .with(new MovementServerSystem(game))
            .with(new DamageServerSystem(game))
            .with(new MeleeAttackSystem(game))
            .with(new RangedAttackSystem(game))
            .with(new ProjectileAttackSystem(game))
            .with(new ProjectileImpactSystem(game))
            .with(new OnCreationServerSystem(game))
            .with(new FreezeServerSystem(game))
            .with(new ProjectileMovementSystem(game))
            .with(new HealSystem(game))
            .with(new MovementSystem())
            .with(new DamageAndHealSystem())
            .with(new FreezeSystem())
            .with(new OnCreationSystem())
            .with(new VectorApplicationSystem())
            .build();
    }
}

