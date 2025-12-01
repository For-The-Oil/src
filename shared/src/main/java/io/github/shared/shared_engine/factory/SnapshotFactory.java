package io.github.shared.shared_engine.factory;

import com.artemis.Component;
import com.artemis.ComponentMapper;
import com.artemis.Entity;
import com.artemis.World;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.github.shared.data.enumsTypes.EntityType;
import io.github.shared.data.enumsTypes.ResourcesType;
import io.github.shared.data.component.DamageComponent;
import io.github.shared.data.component.NetComponent;
import io.github.shared.data.component.RessourceComponent;
import io.github.shared.data.gameobject.DamageEntry;
import io.github.shared.data.registry.ComponentRegistry;
import io.github.shared.data.snapshot.ComponentSnapshot;
import io.github.shared.data.snapshot.EntitySnapshot;

public final class SnapshotFactory {


    public static Entity toEntity(World world, EntitySnapshot snapshot) {
        Entity entity = world.createEntity();

        // Ajouter NetComponent en premier
        NetComponent net = world.getMapper(NetComponent.class).create(entity);
        net.netId = snapshot.getNetId();
        net.entityType = snapshot.getEntityType();

        for (ComponentSnapshot cs : snapshot.getComponentSnapshot()) {
            try {
                String type = cs.getType();
                Class<?> clazz = Class.forName("io.github.shared.local.data.component." + type);
                Component component = (Component) clazz.getDeclaredConstructor().newInstance();

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
                    case "MoveComponent":
                        for (Map.Entry<String, Object> entry : cs.getFields().entrySet()) {
                            Field field = clazz.getDeclaredField(entry.getKey());
                            field.setAccessible(true);
                            field.set(component, entry.getValue());
                        }
                        break;


                    case "RessourceComponent":
                        RessourceComponent rc = new RessourceComponent();
                        Object rawMap = cs.getFields().get("resources");
                        if (rawMap instanceof Map) {
                            Map<?, ?> map = (Map<?, ?>) rawMap;
                            for (Map.Entry<?, ?> entry : map.entrySet()) {
                                ResourcesType typeKey = ResourcesType.valueOf(entry.getKey().toString());
                                int amount = (int) entry.getValue();
                                rc.add(typeKey, amount);
                            }
                        }
                        component = rc;
                        break;

                    case "DamageComponent":
                        DamageComponent dc = new DamageComponent();
                        Object rawList = cs.getFields().get("entries");
                        if (rawList instanceof List) {
                            List<?> list = (List<?>) rawList;
                            for (Object obj : list) if (obj instanceof DamageEntry) dc.addDamage((DamageEntry) obj);
                        }
                        component = dc;
                        break;

                    default:
                        throw new IllegalArgumentException("Composant non pris en charge : " + type);
                }

                entity.edit().add(component);

            } catch (Exception e) {
                System.out.print("toEntity err "+e);
            }
        }

        return entity;
    }

    public static EntitySnapshot fromEntity(World world, Entity entity) {
        NetComponent net = world.getMapper(NetComponent.class).get(entity);
        int netId = net.netId;
        EntityType entityType = net.entityType;

        ArrayList<ComponentSnapshot> componentSnapshots = new ArrayList<>();

        for (Class<? extends Component> clazz : ComponentRegistry.registeredComponents) {
            if (clazz == NetComponent.class) continue;
            try {
                ComponentMapper<?> mapper = world.getMapper(clazz);
                if (mapper.has(entity)) {
                    Component component = mapper.get(entity);
                    HashMap<String, Object> fields = new HashMap<>();
                    String type = clazz.getSimpleName();

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
                        case "MoveComponent":
                            for (Field field : clazz.getDeclaredFields()) {
                                field.setAccessible(true);
                                try {
                                    fields.put(field.getName(), field.get(component));
                                } catch (IllegalAccessException e) {
                                    System.err.println("Erreur d'accès au champ " + field.getName() + " du composant " + type);
                                }
                            }
                            break;
                        case "RessourceComponent":
                            RessourceComponent rc = (RessourceComponent) component;
                            fields.put("resources", new HashMap<>(rc.getAll()));
                            break;

                        case "DamageComponent":
                            DamageComponent dc = (DamageComponent) component;
                            fields.put("entries", new ArrayList<>(dc.entries));
                            break;

                        default:
                            throw new IllegalArgumentException("Composant non pris en charge : " + type);
                    }

                    componentSnapshots.add(new ComponentSnapshot(type, fields));
                }
            } catch (Exception e) {
                System.out.print("toEntity err "+e);
            }
        }
        return new EntitySnapshot(netId, entityType, componentSnapshots);
    }


}
