package io.github.shared.shared_engine.factory;

import java.util.UUID;

import io.github.shared.data.EnumsTypes.Direction;
import io.github.shared.data.EnumsTypes.EntityType;
import io.github.shared.data.EnumsTypes.WeaponType;
import io.github.shared.data.component.BuildingMapPositionComponent;
import io.github.shared.data.component.FreezeComponent;
import io.github.shared.data.component.LifeComponent;
import io.github.shared.data.component.MeleeAttackComponent;
import io.github.shared.data.component.MoveComponent;
import io.github.shared.data.component.OnCreationComponent;
import io.github.shared.data.component.PositionComponent;
import io.github.shared.data.component.ProjectileAttackComponent;
import io.github.shared.data.component.ProjectileComponent;
import io.github.shared.data.component.ProprietyComponent;
import io.github.shared.data.component.RangedAttackComponent;
import io.github.shared.data.component.SpeedComponent;
import io.github.shared.data.component.TargetComponent;
import io.github.shared.data.component.VelocityComponent;

public final class ComponentFactory {

    public static FreezeComponent freezeComponent(EntityType entityType) {
        FreezeComponent freezeComponent = new FreezeComponent();
        freezeComponent.freeze_time = entityType.getFreeze_time();
        return freezeComponent;
    }

    public static LifeComponent lifeComponent(EntityType entityType) {
        LifeComponent lifeComponent = new LifeComponent();
        lifeComponent.maxHealth = entityType.getMaxHealth();
        lifeComponent.health = entityType.getMaxHealth();
        lifeComponent.armor = entityType.getArmor();
        lifeComponent.passiveHeal = entityType.getPassiveHeal();
        return lifeComponent;
    }

    public static MeleeAttackComponent meleeAttackComponent(WeaponType weaponType) {
        MeleeAttackComponent attackComponent = new MeleeAttackComponent();
        attackComponent.weaponType = weaponType;
        attackComponent.damage = weaponType.getDamage();
        attackComponent.cooldown = weaponType.getCooldown();
        attackComponent.currentCooldown = 0f;
        attackComponent.reach = weaponType.getReach();
        return attackComponent;
    }

    public static ProjectileAttackComponent projectileAttackComponent(WeaponType weaponType, EntityType projectileType) {
        ProjectileAttackComponent projectileAttackComponent = new ProjectileAttackComponent();
        projectileAttackComponent.weaponType = weaponType;
        projectileAttackComponent.cooldown = weaponType.getCooldown();
        projectileAttackComponent.currentCooldown = 0f;
        projectileAttackComponent.range = weaponType.getReach();
        projectileAttackComponent.projectileType = projectileType;
        return projectileAttackComponent;
    }

    public static ProjectileComponent projectileComponent(EntityType projectileType) {
        ProjectileComponent projectileComponent = new ProjectileComponent();
        projectileComponent.projectileType = projectileType;
        projectileComponent.damage = projectileType.getDamage();
        projectileComponent.aoe = projectileType.getAoe();
        projectileComponent.maxHeight = projectileType.getMaxHeight();
        return projectileComponent;
    }

    public static RangedAttackComponent rangedAttackComponent(WeaponType weaponType) {
        RangedAttackComponent rangedAttackComponent = new RangedAttackComponent();
        rangedAttackComponent.weaponType = weaponType;
        rangedAttackComponent.damage = weaponType.getDamage();
        rangedAttackComponent.cooldown = weaponType.getCooldown();
        rangedAttackComponent.currentCooldown = 0f;
        rangedAttackComponent.range = weaponType.getReach();
        return rangedAttackComponent;
    }

    public static SpeedComponent speedComponent(EntityType entityType) {
        SpeedComponent speedComponent = new SpeedComponent();
        speedComponent.base_speed = entityType.getBase_speed();
        return speedComponent;
    }


    public static PositionComponent positionComponent(float x, float y, float z) {
        PositionComponent c = new PositionComponent();
        c.set(x, y, z);
        return c;
    }

    public static VelocityComponent velocityComponent(float vx, float vy) {
        VelocityComponent c = new VelocityComponent();
        c.set(vx, vy);
        return c;
    }

    public static MoveComponent moveComponent(int targetId, float destinationX, float destinationY, float nextX1, float nextY1, float nextX2, float nextY2, boolean force) {
        MoveComponent c = new MoveComponent();
        c.set(destinationX, destinationY, nextX1, nextY1, nextX2, nextY2, force);
        return c;
    }

    public static TargetComponent targetComponent(int targetId, int nextTargetId,boolean force) {
        TargetComponent c = new TargetComponent();
        c.set(targetId, nextTargetId, force);
        return c;
    }

    public static ProprietyComponent proprietyComponent(UUID player, String team) {
        ProprietyComponent c = new ProprietyComponent();
        c.set(player, team);
        return c;
    }

    public static BuildingMapPositionComponent buildingMapPositionComponent(int x, int y, Direction direction) {
        BuildingMapPositionComponent c = new BuildingMapPositionComponent();
        c.set(x, y, direction);
        return c;
    }

    public static OnCreationComponent onCreationComponent(int x, int y, int from, long timeMillis) {
        OnCreationComponent c = new OnCreationComponent();
        c.set(x, y, from,timeMillis);
        return c;
    }


}
