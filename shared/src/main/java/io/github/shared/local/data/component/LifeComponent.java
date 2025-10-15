package io.github.shared.local.data.component;

public class LifeComponent {
    private float health;          // Points de vie actuels
    private final float maxHealth;  // Points de vie max
    private float armor;           // Réduction de dégâts (pourcentage ou valeur fixe)
    private float passiveHeal;     // PV régénérés par seconde

    public LifeComponent(float maxHealth, float armor, float passiveHeal) {
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

    public float getArmor() {
        return armor;
    }

    public void setArmor(float armor) {
        this.armor = armor;
    }

    public float getPassiveHeal() {
        return passiveHeal;
    }

    public void setPassiveHeal(float passiveHeal) {
        this.passiveHeal = passiveHeal;
    }

    /** Applique un soin immédiat */
    public void heal(float amount) {
        setHealth(this.health + amount);
    }

    /** Applique des dégâts en prenant en compte l’armure */
    public void takeDamage(float damage) {
        float effectiveDamage = Math.max(damage - armor, 0);
        setHealth(this.health - effectiveDamage);
    }

    /** Appelé chaque frame ou chaque seconde pour régénérer automatiquement */
    public void update(float deltaTime) {
        heal(passiveHeal * deltaTime);
    }

    public boolean isDead() {
        return health <= 0;
    }
}
