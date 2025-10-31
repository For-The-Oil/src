package io.github.shared.local.data.registry;


import com.artemis.Component;

import java.util.Arrays;
import java.util.List;

import io.github.shared.local.data.component.*;


public class ComponentRegistry {
    public static final List<Class<? extends Component>> registeredComponents = Arrays.asList(
        BuildingMapPositionComponent.class,
        DamageComponent.class,
        DamageEntry.class,
        FreezeComponent.class,
        LifeComponent.class,
        MeleeAttackComponent.class,
        NetComponent.class,
        PositionComponent.class,
        ProjectileAttackComponent.class,
        ProjectileComponent.class,
        ProprietyComponent.class,
        RangedAttackComponent.class,
        RessourceComponent.class,
        SpeedComponent.class,
        TargetComponent.class,
        VelocityComponent.class
    );
}

