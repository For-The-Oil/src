package io.github.shared.local.data.component;

public class LifeComponent {
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

    public float getHealth() {
        return health;
    }

    public void setHealth(float health) {
        this.health = Math.min(health, maxHealth);
    }

    public float getMaxHealth() {
        return maxHealth;
    }

    public int getArmor() {
        return armor;
    }

    public float getPassiveHeal() {
        return passiveHeal;
    }

    public void setPassiveHeal(float passiveHeal) {
        this.passiveHeal = passiveHeal;
    }

    public boolean isDead() {
        return health <= 0;
    }
}
