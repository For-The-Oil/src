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
 * Processes entities with LifeComponent and DamageComponent.
 * Applies damage using exponential armor reduction, clears consumed entries,
 * and enqueues a deferred destroy instruction when the entity is no longer alive.
 *
 * Formula: finalDamage = damage * (ARMOR_COEF ^ max(armor - armorPenetration, 0))
 */
@Wire
public class DamageSystem extends IteratingSystem {

    /** Server used to enqueue deferred destroy instructions. */
    private final ServerGame server;

    // Injected by Artemis (no manual constructor init required)
    private ComponentMapper<LifeComponent> mLife;
    private ComponentMapper<DamageComponent> mDamage;
    private ComponentMapper<NetComponent> mNet;

    /**
     * @param server ServerGame instance to add destroy instructions.
     */
    public DamageSystem(ServerGame server) {
        super(Aspect.all(LifeComponent.class, DamageComponent.class, NetComponent.class));
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
        DamageComponent dmg = mDamage.get(entityId);
        if (!dmg.hasDamage()) return;

        float totalDamage = 0f;
        for (DamageEntry entry : dmg.entries) {
            int armorResidual = Math.max(life.armor - Math.round(entry.armorPenetration), 0);
            totalDamage += (float) (entry.damage * Math.pow(BaseGameConfig.ARMOR_COEF, armorResidual));
        }

        life.takeDamage(totalDamage);
        dmg.clear();

        if (!life.isAlive()) {
            NetComponent net = mNet.get(entityId);
            if (net != null && net.isValid()) {
                server.addDestroyInstruction(net.netId);
            }
        }
    }
}


