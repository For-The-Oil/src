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

}
