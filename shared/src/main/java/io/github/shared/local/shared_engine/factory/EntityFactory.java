package io.github.shared.local.shared_engine.factory;

import com.artemis.Component;
import com.artemis.ComponentMapper;
import com.artemis.Entity;
import com.artemis.World;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

import io.github.shared.local.data.EnumsTypes.RessourcesType;
import io.github.shared.local.data.component.DamageComponent;
import io.github.shared.local.data.component.RessourceComponent;
import io.github.shared.local.data.gameobject.DamageEntry;
import io.github.shared.local.data.snapshot.ComponentSnapshot;
import io.github.shared.local.data.snapshot.EntitySnapshot;

public final class EntityFactory {


    public static void applySnapshotToEntity(World world, Entity entity, EntitySnapshot snapshot) {
        for (ComponentSnapshot cs : snapshot.getComponentSnapshot()) {
            try {
                Class<?> clazz = Class.forName("io.github.shared.local.data.component." + cs.getType());
                ComponentMapper<?> mapper = world.getMapper(clazz.asSubclass(Component.class));
                Component component;

                if (mapper.has(entity)) {
                    component = mapper.get(entity); // composant existant
                } else {
                    component = (Component) clazz.getDeclaredConstructor().newInstance(); // nouveau composant
                }

                String type = cs.getType();

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
                        for (Map.Entry<String, Object> entry : cs.getFields().entrySet()) {
                            Field field = clazz.getDeclaredField(entry.getKey());
                            field.setAccessible(true);
                            field.set(component, entry.getValue());
                        }
                        break;

                    case "RessourceComponent":
                        RessourceComponent rc = (RessourceComponent) component;
                        Object rawMap = cs.getFields().get("ressources");
                        if (rawMap instanceof Map) {
                            Map<?, ?> map = (Map<?, ?>) rawMap;
                            rc.reset(); // pour éviter les doublons
                            for (Map.Entry<?, ?> entry : map.entrySet()) {
                                RessourcesType typeKey = RessourcesType.valueOf(entry.getKey().toString());
                                int amount = (int) entry.getValue();
                                rc.add(typeKey, amount);
                            }
                        }
                        break;

                    case "DamageComponent":
                        DamageComponent dc = (DamageComponent) component;
                        Object rawList = cs.getFields().get("entries");
                        if (rawList instanceof List) {
                            List<?> list = (List<?>) rawList;
                            dc.clear(); // pour éviter les doublons
                            for (Object obj : list) if (obj instanceof DamageEntry) dc.addDamage((DamageEntry) obj);
                        }
                        break;
                    default:
                        throw new IllegalArgumentException("Composant non pris en charge : " + type);
                }

                entity.edit().add(component);

            } catch (Exception e) {
                System.err.println("Erreur lors de l'application du snapshot pour le composant : " + cs.getType());
                e.printStackTrace();
            }
        }
    }

}
