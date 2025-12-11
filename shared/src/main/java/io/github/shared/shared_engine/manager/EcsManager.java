package io.github.shared.shared_engine.manager;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.Entity;
import com.artemis.World;
import com.artemis.utils.IntBag;

import java.util.ArrayList;
import java.util.UUID;

import io.github.shared.data.component.PositionComponent;
import io.github.shared.data.enums_types.EntityType;
import io.github.shared.data.component.NetComponent;
import io.github.shared.data.component.ProprietyComponent;
import io.github.shared.data.snapshot.EntitySnapshot;

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



    /**
     * Find the first entity in the World whose NetComponent.netId == netId.
     * Subset is minimal: entities that have NetComponent.
     *
     * @param world Artemis world (must not be null)
     * @param netId target network id to search for
     * @return the matching Entity, or null if none found
     */
    public static Entity findEntityByNetId(World world, int netId) {
        if (world == null) return null;

        ComponentMapper<NetComponent> netMapper = world.getMapper(NetComponent.class);

        // Iterate only entities that have NetComponent (lighter than adding other constraints here)
        IntBag bag = world.getAspectSubscriptionManager()
            .get(Aspect.all(NetComponent.class))
            .getEntities();

        int[] ids = bag.getData();
        for (int i = 0, n = bag.size(); i < n; i++) {
            int eid = ids[i];
            NetComponent net = netMapper.get(eid);
            if (net != null && net.netId == netId) {
                // Early return: take the first match for this netId
                return world.getEntity(eid);
            }
        }
        return null;
    }

    /**
     * Find the entity that satisfies:
     *  - NetComponent.netId == netId
     *  - ProprietyComponent.player == playerId
     *
     * Implementation detail:
     *  - Calls findEntityByNetId(...) first, then validates the player.
     *  - Early-return behavior is preserved: if the first entity with this netId
     *    does not match the player, this method returns null without scanning further.
     *
     * @param world Artemis world
     * @param netId network id to match
     * @param playerId expected player UUID (must not be null)
     * @return the matching Entity, or null if no match
     */
    public static Entity findEntityByNetIdAndPlayer(World world, int netId, UUID playerId) {
        if (world == null || playerId == null) return null;
        Entity e = findEntityByNetId(world, netId);
        if (e == null) return null;

        ComponentMapper<ProprietyComponent> propMapper = world.getMapper(ProprietyComponent.class);
        ProprietyComponent prop = propMapper.get(e);
        return (prop != null && playerId.equals(prop.player)) ? e : null;
    }

    /**
     * Find the entity that satisfies:
     *  - NetComponent.netId == netId
     *  - If entityType != null, NetComponent.entityType == entityType
     *  - ProprietyComponent.player == playerId
     *
     * Implementation detail:
     *  - Uses findEntityByNetId(...) to grab the candidate first.
     *  - If entityType is provided (non-null), it must exactly match; otherwise, type is ignored.
     *  - Early-return behavior is preserved: if the candidate's type (when provided) or player
     *    does not match, returns null immediately (does not look for other entities with same netId).
     *
     * @param world Artemis world
     * @param netId network id to match
     * @param playerId expected player UUID (must not be null)
     * @param entityType expected entity type (nullable: when null, type is ignored)
     * @return the matching Entity, or null if no match
     */
    public static Entity findEntityByNetIdPlayerAndEntityType(World world, int netId, UUID playerId, EntityType entityType)
    {
        if (world == null || playerId == null) return null;

        // Reuse the player check first; returns null if netId not found or player mismatch
        Entity e = findEntityByNetIdAndPlayer(world, netId, playerId);
        if (e == null) return null;

        // If type is provided, enforce equality; otherwise, ignore
        if (entityType != null) {
            ComponentMapper<NetComponent> netMapper = world.getMapper(NetComponent.class);
            NetComponent net = netMapper.get(e);
            if (net == null || net.entityType != entityType) {
                return null;
            }
        }
        return e;
    }

    /**
     * Find the entity that satisfies:
     *  - NetComponent.netId == netId
     *  - If entityType != null, NetComponent.entityType == entityType
     *  - ProprietyComponent.player == playerId
     *
     * Implementation detail:
     *  - Uses findEntityByNetId(...) to grab the candidate first.
     *  - If entityType is provided (non-null), it must exactly match; otherwise, type is ignored.
     *  - Early-return behavior is preserved: if the candidate's type (when provided) or player
     *    does not match, returns null immediately (does not look for other entities with same netId).
     *
     * @param world Artemis world
     * @param netId network id to match
     * @param playerId expected player UUID (must not be null)
     * @param type expected entity.type (nullable: when null, type is ignored)
     * @return the matching Entity, or null if no match
     */
    public static Entity findEntityByNetIdPlayerAndType(World world, int netId, UUID playerId, EntityType.Type type)
    {
        if (world == null || playerId == null) return null;

        // Reuse the player check first; returns null if netId not found or player mismatch
        Entity e = findEntityByNetIdAndPlayer(world, netId, playerId);
        if (e == null) return null;

        // If type is provided, enforce equality; otherwise, ignore
        if (type != null) {
            ComponentMapper<NetComponent> netMapper = world.getMapper(NetComponent.class);
            NetComponent net = netMapper.get(e);
            if (net == null || net.entityType.getType() != type) {
                return null;
            }
        }
        return e;
    }

    public static ArrayList<Integer> extractNetIds(ArrayList<EntitySnapshot> snapshots) {
        ArrayList<Integer> netIds = new ArrayList<>();
        if (snapshots == null) return netIds;

        for (EntitySnapshot snapshot : snapshots) {
            if (snapshot != null) {
                netIds.add(snapshot.getNetId());
            }
        }
        return netIds;
    }

    public static PositionComponent getPositionByNetId(World world, int netId, ComponentMapper<NetComponent> mNet, ComponentMapper<PositionComponent> mPos) {
        if (world == null || netId < 0) return null;

        // Récupère tous les entités qui possèdent NetComponent
        IntBag entities = world.getAspectSubscriptionManager().get(Aspect.all(NetComponent.class,PositionComponent.class)).getEntities();

        int[] ids = entities.getData();
        for (int i = 0, size = entities.size(); i < size; i++) {
            int eId = ids[i];
            NetComponent net = mNet.get(eId);
            if (net != null && net.netId == netId && net.isValid()) { // netId + validité
                return mPos.get(eId);
            }
        }
        return null; // pas trouvé
    }

    public static int getIdByNetId(World world, int netId, ComponentMapper<NetComponent> mNet) {
        if (world == null || netId < 0) return -1;

        // Récupère tous les entités qui possèdent NetComponent
        IntBag entities = world.getAspectSubscriptionManager().get(Aspect.all(NetComponent.class)).getEntities();

        int[] ids = entities.getData();
        for (int i = 0, size = entities.size(); i < size; i++) {
            int eId = ids[i];
            NetComponent net = mNet.get(eId);
            if (net != null && net.netId == netId && net.isValid()) { // netId + validité
                return eId;
            }
        }
        return -1; // pas trouvé
    }




}
