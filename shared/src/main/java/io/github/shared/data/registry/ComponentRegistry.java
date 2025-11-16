package io.github.shared.data.registry;


import com.artemis.Component;

import java.util.Arrays;
import java.util.List;

import io.github.shared.data.component.*;


public class ComponentRegistry {
    public static final List<Class<? extends Component>> registeredComponents = Arrays.asList(
        BuildingMapPositionComponent.class,
        DamageComponent.class,
        FreezeComponent.class,
        LifeComponent.class,
        MeleeAttackComponent.class,
        NetComponent.class,
        OnCreationComponent.class,
        PositionComponent.class,
        ProjectileAttackComponent.class,
        ProjectileComponent.class,
        ProprietyComponent.class,
        RangedAttackComponent.class,
        RessourceComponent.class,
        SpeedComponent.class,
        TargetComponent.class,
        VelocityComponent.class,
        MoveComponent.class
    );
}

