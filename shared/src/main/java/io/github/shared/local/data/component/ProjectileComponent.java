package io.github.shared.local.data.component;


import com.artemis.Component;

import com.artemis.annotations.PooledWeaver;

import io.github.shared.local.data.EnumsTypes.ProjectileType;


@PooledWeaver
public class ProjectileComponent extends Component {
    public ProjectileType projectileType;
    public int damage;
    public float aoe;
    public float maxHeight;

    public void reset() {
        projectileType = null;
        damage = 0;
        aoe = 0f;
        maxHeight = 0f;
    }

    public void set(ProjectileType type, int damage, float aoe, float maxHeight) {
        this.projectileType = type;
        this.damage = damage;
        this.aoe = aoe;
        this.maxHeight = maxHeight;
    }
}

