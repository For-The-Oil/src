package io.github.shared.data.gameobject;


import java.io.Serializable;

public class DamageEntry implements Serializable {
    public int sourceEntityId;
    public float damage;
    public float armorPenetration;

    public DamageEntry() {}

    public DamageEntry(int sourceEntityNetId, float damage, float armorPenetration) {
        this.sourceEntityId = sourceEntityNetId;
        this.damage = damage;
        this.armorPenetration = armorPenetration;
    }
}


