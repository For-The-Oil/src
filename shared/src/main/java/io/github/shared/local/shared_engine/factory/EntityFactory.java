package io.github.shared.local.shared_engine.factory;

import com.artemis.Component;
import com.artemis.ComponentMapper;
import com.artemis.Entity;
import com.artemis.World;

import java.lang.reflect.Field;
import java.util.Map;

import io.github.shared.local.data.snapshot.ComponentSnapshot;
import io.github.shared.local.data.snapshot.EntitySnapshot;

public final class EntityFactory {

    public static void applySnapshotToEntity(World world, Entity entity, EntitySnapshot snapshot) {
        for (ComponentSnapshot cs : snapshot.getComponentSnapshot()) {
            try {
                // Trouve la classe du composant
                Class<?> clazz = Class.forName("io.github.shared.local.data.component." + cs.getType());

                // Vérifie si l'entité possède déjà ce composant
                ComponentMapper<?> mapper = world.getMapper(clazz.asSubclass(Component.class));
                Component component;

                if (mapper.has(entity)) {
                    component = mapper.get(entity); // composant existant
                } else {
                    component = (Component) clazz.getDeclaredConstructor().newInstance(); // nouveau composant
                }

                // Met à jour les champs du composant
                for (Map.Entry<String, Object> entry : cs.getFields().entrySet()) {
                    Field field = clazz.getDeclaredField(entry.getKey());
                    field.setAccessible(true);
                    field.set(component, entry.getValue());
                }
                entity.edit().add(component);

            } catch (Exception e) {
                System.err.println("Erreur lors de l'application du snapshot pour le composant : " + cs.getType());
                e.printStackTrace();
            }
        }
    }

}
