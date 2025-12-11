package io.github.server.game_engine.manager;

import com.artemis.Entity;
import com.artemis.World;
import com.artemis.ComponentMapper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

import io.github.shared.data.component.VelocityComponent;
import io.github.shared.data.enums_types.ResourcesType;
import io.github.shared.data.component.NetComponent;
import io.github.shared.data.enums_types.EntityType;
import io.github.shared.data.instructions.UpdateEntityInstruction;
import io.github.shared.data.snapshot.ComponentSnapshot;
import io.github.shared.data.snapshot.EntitySnapshot;
import io.github.shared.shared_engine.Utility;
import io.github.shared.shared_engine.manager.EcsManager;

/**
 * SnapshotTracker
 *
 * Tracks per-entity component snapshots that should be sent to clients or
 * consumed by server-side update instructions. Callers provide ready-made
 * ComponentSnapshot instances and the tracker aggregates them by entity.
 *
 * Usage:
 * - Call markComponentModified(entity, componentSnapshot) whenever a component
 *   changes and you want that change reflected in the outgoing snapshots.
 * - Periodically call consumeSnapshots() or consumeUpdateInstruction(timestamp)
 *   to retrieve and clear the pending snapshots.
 */
public class SnapshotTracker {

    /** Pending snapshots keyed by entity netId. */
    private final Map<Integer, EntitySnapshot> pendingSnapshots = new HashMap<>();

    /**
     * Registers a component change for the given entity.
     * The caller supplies a ready-made ComponentSnapshot (type + fields).
     *
     * Steps:
     * 1) Resolve the entity's NetComponent to obtain netId and entityType.
     * 2) Insert or merge the supplied ComponentSnapshot into the entity's snapshot.
     *
     * @param entity            Artemis entity whose component changed
     * @param componentSnapshot Snapshot describing the changed component (type and fields)
     */
    public void markComponentModified(Entity entity, ComponentSnapshot componentSnapshot) {
        if (entity == null || componentSnapshot == null) return;

        World world = entity.getWorld();
        if (world == null) return;

        // Resolve NetComponent to identify the entity in the network layer
        ComponentMapper<NetComponent> netMapper = world.getMapper(NetComponent.class);
        if (netMapper == null || !netMapper.has(entity)) return;

        NetComponent net = netMapper.get(entity);
        int entityId = net.netId;
        EntityType entityType = net.entityType;

        String type = componentSnapshot.getType();
        if (type == null || type.isEmpty()) return;

        // Insert or merge snapshot into the per-entity aggregate
        EntitySnapshot currentSnapshot = pendingSnapshots.get(entityId);
        if (currentSnapshot == null) {
            ArrayList<ComponentSnapshot> components = new ArrayList<>();
            components.add(componentSnapshot);
            pendingSnapshots.put(entityId, new EntitySnapshot(entityId, entityType, components));
            return;
        }

        ComponentSnapshot previousSnapshot = currentSnapshot.getComponentSnapshot()
            .stream()
            .filter(cs -> type.equals(cs.getType()))
            .findFirst()
            .orElse(null);
        if (previousSnapshot != null) {
            // Merge according to component type
            handleDependentComponent(type, previousSnapshot, componentSnapshot);
        } else {
            currentSnapshot.getComponentSnapshot().add(componentSnapshot);
        }
    }

    public ComponentSnapshot getPreviousSnapshot(Entity entity, String type) {
        if (entity == null) return null;
        World world = entity.getWorld();
        if (world == null) return null;
        ComponentMapper<NetComponent> netMapper = world.getMapper(NetComponent.class);
        if (netMapper == null || !netMapper.has(entity)) return null;
        NetComponent net = netMapper.get(entity);
        int entityId = net.netId;
        EntitySnapshot currentSnapshot = pendingSnapshots.get(entityId);
        if (currentSnapshot == null) {return null;}
        return currentSnapshot.getComponentSnapshot()
            .stream()
            .filter(cs -> type.equals(cs.getType()))
            .findFirst()
            .orElse(null);
    }

    /**
     * Merges a new snapshot into a previous snapshot, depending on the component type.
     *
     * Rules:
     * - Most components: simple overwrite of fields.
     * - DamageComponent: merge "entries" lists by concatenation.
     * - VelocityComponent: sum vx, vy, vz if present in both snapshots.
     *
     * @param typeName         Component type name (e.g., "PositionComponent")
     * @param previousSnapshot Previously stored snapshot to update
     * @param newSnapshot      Newly supplied snapshot to merge
     */
    private void handleDependentComponent(String typeName, ComponentSnapshot previousSnapshot, ComponentSnapshot newSnapshot) {
        try {
            switch (typeName) {
                // Simple overwrite set
                case "LifeComponent":
                case "FreezeComponent":
                case "SpeedComponent":
                case "MeleeAttackComponent":
                case "RangedAttackComponent":
                case "ProprietyComponent":
                case "NetComponent":
                case "PositionComponent":
                case "ProjectileComponent":
                case "ProjectileAttackComponent":
                case "TargetComponent":
                case "BuildingMapPositionComponent":
                case "OnCreationComponent":
                case "MoveComponent": {
                    previousSnapshot.setFields(newSnapshot.getFields());
                    break;
                }

                // Merge damage entries (append lists)
                case "DamageComponent": {
                    List<?> prevEntries = safeList(previousSnapshot.getFields().get("entries"));
                    List<?> newEntries  = safeList(newSnapshot.getFields().get("entries"));
                    List<Object> merged = new ArrayList<>();
                    if (prevEntries != null) merged.addAll(prevEntries);
                    if (newEntries  != null) merged.addAll(newEntries);
                    previousSnapshot.getFields().put("entries", merged);
                    break;
                }

                // Sum velocity components if present (supports vx, vy, vz)
                case "VelocityComponent": {
                    Float prevVx = asFloat(previousSnapshot.getFields().get("vx"));
                    Float prevVy = asFloat(previousSnapshot.getFields().get("vy"));
                    Float prevVz = asFloat(previousSnapshot.getFields().get("vz"));
                    Float newVx  = asFloat(newSnapshot.getFields().get("vx"));
                    Float newVy  = asFloat(newSnapshot.getFields().get("vy"));
                    Float newVz  = asFloat(newSnapshot.getFields().get("vz"));

                    float mergedVx = (prevVx != null ? prevVx : 0f) + (newVx != null ? newVx : 0f);
                    float mergedVy = (prevVy != null ? prevVy : 0f) + (newVy != null ? newVy : 0f);
                    float mergedVz = (prevVz != null ? prevVz : 0f) + (newVz != null ? newVz : 0f);

                    previousSnapshot.getFields().put("vx", mergedVx);
                    previousSnapshot.getFields().put("vy", mergedVy);
                    previousSnapshot.getFields().put("vz", mergedVz);
                    break;
                }

                case "RessourceComponent": {
                    // Additionne les ressources clé par clé
                    Map<String, Object> prevMap = previousSnapshot.getFields();
                    Map<String, Object> newMap  = newSnapshot.getFields();

                    Object prevResourcesObj = prevMap.get("resources");
                    Object newResourcesObj  = newMap.get("resources");

                    if (prevResourcesObj instanceof Map && newResourcesObj instanceof Map) {
                        @SuppressWarnings("unchecked")
                        Map<ResourcesType, Integer> prevResources = (Map<ResourcesType, Integer>) prevResourcesObj;
                        @SuppressWarnings("unchecked")
                        Map<ResourcesType, Integer> newResources  = (Map<ResourcesType, Integer>) newResourcesObj;

                        for (Map.Entry<ResourcesType, Integer> entry : newResources.entrySet()) {
                            ResourcesType key = entry.getKey();
                            int addVal = entry.getValue() != null ? entry.getValue() : 0;
                            int oldVal = prevResources.getOrDefault(key, 0);
                            prevResources.put(key, oldVal + addVal);

                        }
                    }
                    break;
                }


                default: {
                    // Fallback: overwrite fields
                    previousSnapshot.setFields(newSnapshot.getFields());
                }
            }
        } catch (Exception e) {
            System.out.print("handleDependentComponent error " + e);
        }
    }

    /**
     * Returns and clears the collection of pending entity snapshots.
     *
     * @return collection of snapshots to be processed or sent
     */
    public Collection<EntitySnapshot> consumeSnapshots() {
        Collection<EntitySnapshot> result = new ArrayList<>(pendingSnapshots.values());
        pendingSnapshots.clear();
        return result;
    }

    /**
     * Indicates whether there are pending snapshots.
     *
     * @return true if no snapshots are pending, false otherwise
     */
    public boolean snapshotsIsEmpty() {
        return pendingSnapshots.isEmpty();
    }

    /**
     * Builds an UpdateEntityInstruction from the current pending snapshots,
     * assigns the given timestamp, and clears the internal buffer.
     *
     * @param timestamp instruction timestamp in milliseconds
     * @return a populated UpdateEntityInstruction
     */
    public UpdateEntityInstruction consumeUpdateInstruction(long timestamp,World world) {
        Collection<EntitySnapshot> snapshots = this.consumeSnapshots();
        Collection<EntitySnapshot> removeSnapshots = new ArrayList<>();
        for (EntitySnapshot entitySnapshot : snapshots){
            for (ComponentSnapshot componentSnapshot : entitySnapshot.getComponentSnapshot()) {
                if(componentSnapshot.getType().equals("VelocityComponent")){
                    int e = EcsManager.getIdByNetId(world,entitySnapshot.getNetId(),world.getMapper(NetComponent.class));
                    VelocityComponent vc = world.getMapper(VelocityComponent.class).get(e);
                    if(vc != null &&
                        (vc.vx == (float)componentSnapshot.getFields().get("vx")) &&
                        (vc.vy == (float)componentSnapshot.getFields().get("vy")) &&
                        (vc.vz == (float)componentSnapshot.getFields().get("vz"))) {
                        removeSnapshots.add(entitySnapshot);
                    }
                }
            }
        }
        snapshots.removeAll(removeSnapshots);
        UpdateEntityInstruction instruction = new UpdateEntityInstruction(timestamp);
        instruction.setToUpdate(new ArrayList<>(snapshots));
        return instruction;
    }

    // -------- Helpers --------

    private static List<?> safeList(Object o) {
        if (o instanceof List<?>) return (List<?>) o;
        return null;
    }

    private static Float asFloat(Object o) {
        if (o instanceof Float) return (Float) o;
        if (o instanceof Number) return ((Number) o).floatValue();
        return null;
    }
}
