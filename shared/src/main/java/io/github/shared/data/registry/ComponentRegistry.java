package io.github.shared.data.registry;


import com.artemis.Component;

import java.util.Arrays;
import java.util.List;

import io.github.shared.data.component.BuildingMapPositionComponent;
import io.github.shared.data.component.DamageComponent;
import io.github.shared.data.component.FreezeComponent;
import io.github.shared.data.component.LifeComponent;
import io.github.shared.data.component.MeleeAttackComponent;
import io.github.shared.data.component.NetComponent;
import io.github.shared.data.component.OnCreationComponent;
import io.github.shared.data.component.PositionComponent;
import io.github.shared.data.component.ProjectileAttackComponent;
import io.github.shared.data.component.ProjectileComponent;
import io.github.shared.data.component.ProprietyComponent;
import io.github.shared.data.component.RangedAttackComponent;
import io.github.shared.data.component.RessourceComponent;
import io.github.shared.data.component.SpeedComponent;
import io.github.shared.data.component.TargetComponent;
import io.github.shared.data.component.VelocityComponent;
import io.github.shared.local.data.component.*;


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
        VelocityComponent.class
    );
}

