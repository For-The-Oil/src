package io.github.server.game_engine.system;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.annotations.Wire;
import com.artemis.systems.IteratingSystem;

import io.github.shared.data.component.LifeComponent;
import io.github.shared.data.component.DamageComponent;
import io.github.shared.data.component.NetComponent;
import io.github.shared.data.gameobject.DamageEntry;
import io.github.shared.config.BaseGameConfig;
import io.github.server.data.ServerGame;

/**
 * Processes entities with LifeComponent and deferred destroy instruction when the entity is no longer alive.
 *
 * Formula: finalDamage = damage * (ARMOR_COEF ^ max(armor - armorPenetration, 0))
 */
@Wire
public class DamageServerSystem extends IteratingSystem {

    /** Server used to enqueue deferred destroy instructions. */
    private final ServerGame server;

    // Injected by Artemis (no manual constructor init required)
    private ComponentMapper<LifeComponent> mLife;
    private ComponentMapper<NetComponent> mNet;

    /**
     * @param server ServerGame instance to add destroy instructions.
     */
    public DamageServerSystem(ServerGame server) {
        super(Aspect.all(LifeComponent.class, NetComponent.class));
        this.server = server;
    }

    /**
     * Aggregates reduced damage, applies it, clears entries,
     * and adds a deferred destroy instruction if the entity is dead.
     *
     * @param entityId Artemis entity ID.
     */
    @Override
    protected void process(int entityId) {
        LifeComponent life = mLife.get(entityId);
        if (!life.isAlive()) {
            NetComponent net = mNet.get(entityId);
            if (net != null && net.isValid()) {
                server.addDestroyInstruction(net.netId);
            }
        }
    }
}


