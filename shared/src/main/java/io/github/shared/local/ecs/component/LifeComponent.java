package io.github.shared.local.ecs.component;

import com.badlogic.ashley.core.Component;

public class LifeComponent implements Component {
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
