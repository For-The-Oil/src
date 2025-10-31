package io.github.shared.local.data.gameobject;


import java.io.Serializable;

public class DamageEntry implements Serializable {
    public int sourceEntityId;
    public float damage;
    public float armorPenetration;

    public DamageEntry() {}

    public DamageEntry(int sourceEntityId, float damage, float armorPenetration) {
        this.sourceEntityId = sourceEntityId;
        this.damage = damage;
        this.armorPenetration = armorPenetration;
    }
}


