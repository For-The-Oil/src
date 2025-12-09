package io.github.shared.shared_engine.system;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.annotations.Wire;
import com.artemis.systems.IteratingSystem;

import io.github.shared.config.BaseGameConfig;
import io.github.shared.data.component.DamageComponent;
import io.github.shared.data.component.LifeComponent;
import io.github.shared.data.component.NetComponent;
import io.github.shared.data.gameobject.DamageEntry;

/**
 * Processes entities with LifeComponent and DamageComponent.
 * Applies damage using exponential armor reduction, clears consumed entries,
 * and enqueues a deferred destroy instruction when the entity is no longer alive.
 *
 * Formula: finalDamage = damage * (ARMOR_COEF ^ max(armor - armorPenetration, 0))
 */
@Wire
public class DamageAndHealSystem extends IteratingSystem {

    /** Server used to enqueue deferred destroy instructions. */

    // Injected by Artemis (no manual constructor init required)
    private ComponentMapper<LifeComponent> mLife;
    private ComponentMapper<DamageComponent> mDamage;

    public DamageAndHealSystem() {
        super(Aspect.all(LifeComponent.class, DamageComponent.class, NetComponent.class));
    }

    /**
     * Aggregates reduced damage, applies it, clears entries,
     *
     * @param entityId Artemis entity ID.
     */
    @Override
    protected void process(int entityId) {
        LifeComponent life = mLife.get(entityId);
        DamageComponent dmg = mDamage.get(entityId);
        if (!dmg.hasDamage()) return;

        float totalDamage = 0f;
        for (DamageEntry entry : dmg.entries) {
            if(entry.damage < 0){
                totalDamage += entry.damage;
            }
            else {
                int armorResidual = Math.max(life.armor - Math.round(entry.armorPenetration), 0);
                totalDamage += (float) (entry.damage * Math.pow(BaseGameConfig.ARMOR_COEF, armorResidual));
            }
        }

        life.takeDamage(totalDamage);
        dmg.clear();
    }
}


