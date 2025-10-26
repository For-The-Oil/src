package io.github.shared.local.data.component;

import com.artemis.Component;

import com.artemis.annotations.PooledWeaver;

@PooledWeaver
public class LifeComponent extends Component {
    private float health;
    private final float maxHealth;
    private final int armor;
    private float passiveHeal;

    public LifeComponent(float maxHealth, int armor, float passiveHeal) {
        this.maxHealth = maxHealth;
        this.health = maxHealth;
        this.armor = armor;
        this.passiveHeal = passiveHeal;
    }

}
