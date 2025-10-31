package io.github.shared.local.data.component;

import com.artemis.Component;

import com.artemis.annotations.PooledWeaver;

import io.github.shared.local.data.EnumsTypes.EntityType;

@PooledWeaver
public class LifeComponent extends Component {
    public float health;
    public final float maxHealth;
    public final int armor;
    public float passiveHeal;

    public LifeComponent(EntityType entityType) {
        this.maxHealth = entityType.getMaxHealth();
        this.health = entityType.getMaxHealth();
        this.armor = entityType.getArmor();
        this.passiveHeal = entityType.getPassiveHeal();
    }

}
