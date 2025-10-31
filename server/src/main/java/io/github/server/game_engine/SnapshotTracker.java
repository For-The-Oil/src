package io.github.server.game_engine;


import com.artemis.Component;
import com.artemis.ComponentMapper;
import com.artemis.Entity;
import com.artemis.World;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import io.github.shared.local.data.component.NetComponent;
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

        for (Field field : componentClass.getDeclaredFields()) {
            field.setAccessible(true);
            try {
                fields.put(field.getName(), field.get(component));
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }

        String type = componentClass.getSimpleName();
        ComponentSnapshot newSnapshot = new ComponentSnapshot(type, fields);

        EntitySnapshot currentSnapshot = pendingSnapshots.get(entityId);

        if (currentSnapshot == null) {
            // Première modification pour cette entité
            ArrayList<ComponentSnapshot> components = new ArrayList<>();
            components.add(newSnapshot);
            pendingSnapshots.put(entityId, new EntitySnapshot(entityId, net.entityType, components));
        } else {
            // Entité déjà modifiée, vérifier si le composant est déjà présent
            ComponentSnapshot previousSnapshot = currentSnapshot.getComponentSnapshot().stream()
                .filter(cs -> cs.getType().equals(type))
                .findFirst()
                .orElse(null);

            if (previousSnapshot != null) {
                // 🔧 Composant déjà modifié → gérer les dépendances
                handleDependentComponent(componentClass, previousSnapshot, newSnapshot);
            } else {
                currentSnapshot.getComponentSnapshot().add(newSnapshot);
            }
        }
    }

    private void handleDependentComponent(Class<? extends Component> componentClass, ComponentSnapshot previousSnapshot, ComponentSnapshot newSnapshot) {
        // Implémente ici ta logique spéciale
        System.out.println("Composant déjà modifié : " + componentClass.getSimpleName());
        System.out.println("Ancien : " + previousSnapshot.getFields());
        System.out.println("Nouveau : " + newSnapshot.getFields());
    }

    public Collection<EntitySnapshot> consumeSnapshots() {
        Collection<EntitySnapshot> result = pendingSnapshots.values();
        pendingSnapshots.clear();
        return result;
    }
}

