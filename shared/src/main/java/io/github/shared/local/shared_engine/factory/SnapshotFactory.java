package io.github.shared.local.shared_engine.factory;

import com.artemis.Component;
import com.artemis.ComponentMapper;
import com.artemis.Entity;
import com.artemis.World;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import io.github.shared.local.data.EnumsTypes.EntityType;
import io.github.shared.local.data.component.NetComponent;
import io.github.shared.local.data.registry.ComponentRegistry;
import io.github.shared.local.data.snapshot.ComponentSnapshot;
import io.github.shared.local.data.snapshot.EntitySnapshot;

public final class SnapshotFactory {

    public static Entity toEntity(World world, EntitySnapshot snapshot) {
        Entity entity = world.createEntity();

        // Ajouter NetComponent en premier
        NetComponent net = world.getMapper(NetComponent.class).create(entity);
        net.netId = snapshot.getNetId();
        net.entityType = snapshot.getEntityType();

        for (ComponentSnapshot cs : snapshot.getComponentSnapshot()) {
            try {
                Class<?> clazz = Class.forName("io.github.shared.local.data.component." + cs.getType());
                Component component = (Component) clazz.getDeclaredConstructor().newInstance();

                for (Map.Entry<String, Object> entry : cs.getFields().entrySet()) {
                    Field field = clazz.getDeclaredField(entry.getKey());
                    field.setAccessible(true);
                    field.set(component, entry.getValue());
                }
                entity.edit().add(component);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return entity;
    }


    public static EntitySnapshot fromEntity(World world, Entity entity) {
        // 1. Récupérer NetComponent
        NetComponent net = world.getMapper(NetComponent.class).get(entity);
        int netId = net.netId;
        EntityType entityType = net.entityType;

        // 2. Préparer la liste des ComponentSnapshot
        ArrayList<ComponentSnapshot> componentSnapshots = new ArrayList<>();

        for (Class<? extends Component> clazz : ComponentRegistry.registeredComponents) {
            // Ne pas inclure NetComponent dans le snapshot
            if (clazz == NetComponent.class) continue;

            ComponentMapper<?> mapper = world.getMapper(clazz);
            if (mapper.has(entity)) {
                Component component = mapper.get(entity);
                HashMap<String, Object> fields = new HashMap<>();

                for (Field field : clazz.getDeclaredFields()) {
                    field.setAccessible(true);
                    try {
                        fields.put(field.getName(), field.get(component));
                    } catch (IllegalAccessException e) {
                        System.err.println("Erreur d'accès au champ " + field.getName() + " du composant " + clazz.getSimpleName());
                        e.printStackTrace();
                    }
                }
                componentSnapshots.add(new ComponentSnapshot(clazz.getSimpleName(), fields));
            }
        }
        return new EntitySnapshot(netId, entityType, componentSnapshots);
    }




}
