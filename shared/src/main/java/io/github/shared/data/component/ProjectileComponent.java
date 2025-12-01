package io.github.shared.data.component;


import com.artemis.PooledComponent;
import com.artemis.annotations.PooledWeaver;

import io.github.shared.data.enumsTypes.EntityType;


@PooledWeaver
public class ProjectileComponent extends PooledComponent {
    public EntityType projectileType;
    public int damage;
    public float aoe;
    public float maxHeight;
    public float fromX;
    public float fromY;

    @Override
    public void reset() {
        projectileType = null;
        damage = 0;
        aoe = 0f;
        maxHeight = 0f;
        fromX = 0f;
        fromX = 0f;
    }

    public void set(EntityType type, int damage, float aoe, float maxHeight,float fromX,float fromY) {
        this.projectileType = type;
        this.damage = damage;
        this.aoe = aoe;
        this.maxHeight = maxHeight;
        this.fromX = fromX;
        this.fromY = fromY;
    }
}

