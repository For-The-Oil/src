package io.github.server.game_engine.manager;


import com.artemis.Component;
import com.artemis.ComponentMapper;
import com.artemis.Entity;
import com.artemis.World;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.github.shared.local.data.component.DamageComponent;
import io.github.shared.local.data.component.NetComponent;
import io.github.shared.local.data.component.RessourceComponent;
import io.github.shared.local.data.instructions.UpdateEntityInstruction;
import io.github.shared.local.data.snapshot.ComponentSnapshot;
import io.github.shared.local.data.snapshot.EntitySnapshot;

public class SnapshotTracker {
    private final Map<Integer, EntitySnapshot> pendingSnapshots = new HashMap<>();

    public void markComponentModified(World world, Entity entity, Class<? extends Component> componentClass) {
        NetComponent net = world.getMapper(NetComponent.class).get(entity);
        int entityId = net.netId;
        ComponentMapper<?> mapper = world.getMapper(componentClass);
        if (!mapper.has(entity)) return;

        Component component = mapper.get(entity);
        HashMap<String, Object> fields = new HashMap<>();

        String type = componentClass.getSimpleName();

        switch (type) {
            case "FreezeComponent":
            case "LifeComponent":
            case "MeleeAttackComponent":
            case "NetComponent":
            case "PositionComponent":
            case "ProjectileAttackComponent":
            case "ProjectileComponent":
            case "ProprietyComponent":
            case "RangedAttackComponent":
            case "SpeedComponent":
            case "TargetComponent":
            case "VelocityComponent":
            case "BuildingMapPositionComponent":
            case "OnCreationComponent":
                for (Field field : componentClass.getDeclaredFields()) {
                    field.setAccessible(true);
                    try {
                        fields.put(field.getName(), field.get(component));
                    } catch (IllegalAccessException e) {
                        System.err.println("Erreur d'accès au champ " + field.getName() + " du composant " + type);
                        e.printStackTrace();
                    }
                }
                break;
            case "RessourceComponent":
                RessourceComponent rc = (RessourceComponent) component;
                fields.put("ressources", new HashMap<>(rc.getAll())); // clone pour éviter les effets de bord
                break;
            case "DamageComponent":
                DamageComponent dc = (DamageComponent) component;
                fields.put("entries", new ArrayList<>(dc.entries)); // clone pour éviter les effets de bord
                break;

            default:
                throw new IllegalArgumentException("Composant non pris en charge : " + type);
        }


        ComponentSnapshot newSnapshot = new ComponentSnapshot(type, fields);
        EntitySnapshot currentSnapshot = pendingSnapshots.get(entityId);

        if (currentSnapshot == null) {
            ArrayList<ComponentSnapshot> components = new ArrayList<>();
            components.add(newSnapshot);
            pendingSnapshots.put(entityId, new EntitySnapshot(entityId, net.entityType, components));
        } else {
            ComponentSnapshot previousSnapshot = currentSnapshot.getComponentSnapshot().stream()
                .filter(cs -> cs.getType().equals(type))
                .findFirst()
                .orElse(null);

            if (previousSnapshot != null) {
                handleDependentComponent(componentClass, previousSnapshot, newSnapshot);
            } else {
                currentSnapshot.getComponentSnapshot().add(newSnapshot);
            }
        }
    }


    private void handleDependentComponent(Class<? extends Component> componentClass, ComponentSnapshot previousSnapshot, ComponentSnapshot newSnapshot) {
        String typeName = componentClass.getSimpleName();
        switch (typeName) {
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
            case "RessourceComponent":
            case "OnCreationComponent":
                // Écrasement simple
                previousSnapshot.setFields(newSnapshot.getFields());
                break;
            case "DamageComponent":
                // Fusionner les dégâts
                List<?> previousEntries = (List<?>) previousSnapshot.getFields().get("entries");
                List<?> newEntries = (List<?>) newSnapshot.getFields().get("entries");
                List<Object> merged = new ArrayList<>();
                if (previousEntries != null) merged.addAll(previousEntries);
                if (newEntries != null) merged.addAll(newEntries);
                previousSnapshot.getFields().put("entries", merged);
                break;
            case "VelocityComponent":
                Float prevX = (Float) previousSnapshot.getFields().get("x");
                Float prevY = (Float) previousSnapshot.getFields().get("y");
                Float newX = (Float) newSnapshot.getFields().get("x");
                Float newY = (Float) newSnapshot.getFields().get("y");

                float mergedX = (prevX != null ? prevX : 0f) + (newX != null ? newX : 0f);
                float mergedY = (prevY != null ? prevY : 0f) + (newY != null ? newY : 0f);

                previousSnapshot.getFields().put("x", mergedX);
                previousSnapshot.getFields().put("y", mergedY);
                break;
            default:
                throw new IllegalArgumentException("Composant inconnu : " + typeName);
        }
    }


    public Collection<EntitySnapshot> consumeSnapshots() {
        Collection<EntitySnapshot> result = pendingSnapshots.values();
        pendingSnapshots.clear();
        return result;
    }

    public boolean snapshotsIsEmpty() {
        return pendingSnapshots.isEmpty();
    }


    public UpdateEntityInstruction consumeUpdateInstruction(long timestamp) {
        // Consommer les snapshots existants
        Collection<EntitySnapshot> snapshots = this.consumeSnapshots();

        // Créer l'instruction avec le timestamp
        UpdateEntityInstruction instruction = new UpdateEntityInstruction(timestamp);

        // Convertir la collection en ArrayList et l'associer à l'instruction
        instruction.setToUpdate(new ArrayList<>(snapshots));

        return instruction;
    }



}

