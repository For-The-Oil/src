package io.github.shared.local.data.component;

import com.artemis.Component;
import com.artemis.annotations.PooledWeaver;

@PooledWeaver
public class DamageEntry extends Component {
    public int sourceEntityId;
    public float damage;
    public float armorPenetration;

    public void set(int sourceEntityId, float damage, float armorPenetration) {
        this.sourceEntityId = sourceEntityId;
        this.damage = damage;
        this.armorPenetration = armorPenetration;
    }
}
