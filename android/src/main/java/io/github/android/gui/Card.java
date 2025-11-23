package io.github.android.gui;

import io.github.shared.data.EnumsTypes.DeckCardCategory;
import io.github.shared.data.EnumsTypes.EntityType;
import io.github.shared.data.EnumsTypes.ResourcesType;

import java.util.HashMap;

public class Card {
    private final int imageResId;
    private final String name;
    private final EntityType.Type type;
    private final DeckCardCategory category;
    private final float maxHealth;
    private final int armor;
    private final float passiveHeal;
    private final float baseSpeed;
    private final float freezeTime;
    private final float createTime;
    private final HashMap<ResourcesType, Integer> cost;

    public Card(EntityType entityType, int imageResId) {
        this.name = entityType.name();
        this.imageResId = imageResId;
        this.type = entityType.getType(); // sera toujours Building
        this.category = entityType.getCategory();
        this.maxHealth = entityType.getMaxHealth();
        this.armor = entityType.getArmor();
        this.passiveHeal = entityType.getPassiveHeal();
        this.baseSpeed = entityType.getBase_speed();
        this.freezeTime = entityType.getFreeze_time();
        this.createTime = entityType.getCreate_time();
        this.cost = entityType.getCost();
    }

    // Getters
    public int getImageResId() { return imageResId; }
    public String getName() { return name; }
    public EntityType.Type getType() { return type; }
    public DeckCardCategory getCategory() { return category; }
    public float getMaxHealth() { return maxHealth; }
    public int getArmor() { return armor; }
    public float getPassiveHeal() { return passiveHeal; }
    public float getBaseSpeed() { return baseSpeed; }
    public float getFreezeTime() { return freezeTime; }
    public float getCreateTime() { return createTime; }
    public HashMap<ResourcesType, Integer> getCost() { return cost; }

}
