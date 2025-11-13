package io.github.shared.local.shared_engine.manager;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.World;
import com.artemis.utils.IntBag;

import java.util.ArrayList;

import io.github.shared.local.data.component.NetComponent;

public class EcsManager {

    public static void filterEntitiesByNetId(World world, ArrayList<Integer> allowedNetIds) {
        ComponentMapper<NetComponent> netMapper = world.getMapper(NetComponent.class);

        // Récupérer toutes les entités du World
        IntBag entities = world.getAspectSubscriptionManager()
            .get(Aspect.all())
            .getEntities();

        for (int i = 0; i < entities.size(); i++) {
            int entityId = entities.get(i);
            NetComponent netComp = netMapper.get(entityId);

            // Si l'entité n'est pas dans la liste, on la supprime
            if (netComp == null || !allowedNetIds.contains(netComp.netId)) {
                world.delete(entityId);
            }
        }
    }

}
