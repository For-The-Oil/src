package io.github.shared.local.data.component;

import com.artemis.Component;

import com.artemis.annotations.PooledWeaver;

import io.github.shared.local.data.EnumsTypes.EntityType;


@PooledWeaver
public class LifeComponent extends Component {
    public float health;
    public float maxHealth;
    public int armor;
    public float passiveHeal;

    public void reset() {
        health = maxHealth = 0f;
        armor = 0;
        passiveHeal = 0f;
    }

    public void set(float health, float maxHealth, int armor, float passiveHeal) {
        this.health = health;
        this.maxHealth = maxHealth;
        this.armor = armor;
        this.passiveHeal = passiveHeal;
    }

    public boolean isAlive() {
        return health > 0f;
    }

    public void heal(float amount) {
        health = Math.min(health + amount, maxHealth);
    }

    public void takeDamage(float amount) {
        health = Math.max(health - amount, 0f);
    }
}

