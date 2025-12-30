package io.github.server.game_engine.system;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.annotations.Wire;
import com.artemis.systems.IteratingSystem;

import java.util.HashMap;

import io.github.shared.data.component.LifeComponent;
import io.github.shared.data.component.NetComponent;
import io.github.shared.data.component.ProprietyComponent;
import io.github.shared.data.enums_types.ResourcesType;
import io.github.server.data.ServerGame;
import io.github.shared.data.instructions.ResourcesInstruction;
import io.github.shared.shared_engine.Utility;
import io.github.shared.shared_engine.manager.EcsManager;

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

    private ComponentMapper<ProprietyComponent> mProp;

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
                if(life.LastHitNetId >= 0) {
                    HashMap<ResourcesType, Integer> payload = new HashMap<>();
                    for (ResourcesType resourcesType : net.entityType.getCost().keySet()) {
                        payload.put(resourcesType, net.entityType.getCost().get(resourcesType) / 2);
                    }
                    int eHit = EcsManager.getIdByNetId(world,life.LastHitNetId,mNet);
                    if (eHit > 0) {
                        ProprietyComponent prop = mProp.get(eHit);
                        if (!payload.isEmpty() && prop != null && prop.player != null) {
                            HashMap<ResourcesType, Integer> playerR = Utility.findPlayerByUuid(server.getPlayersList(), prop.player).getResources();
                            for (ResourcesType resourcesType : playerR.keySet()) {
                                payload.put(resourcesType, playerR.get(resourcesType) + payload.getOrDefault(resourcesType, 0));
                            }
                            // Emit one ResourcesInstruction
                            long timestamp = System.currentTimeMillis();
                            ResourcesInstruction instruction = new ResourcesInstruction(timestamp, payload, prop.player);
                            server.addQueueInstruction(instruction);
                        }
                    }
                }
                server.addDestroyInstruction(net.netId);
            }
        }
    }
}


