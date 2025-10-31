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

}
