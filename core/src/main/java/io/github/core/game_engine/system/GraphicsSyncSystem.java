package io.github.core.game_engine.system;


import com.artemis.Aspect;
import com.artemis.BaseSystem;
import com.artemis.ComponentMapper;
import com.artemis.utils.IntBag;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

import io.github.core.data.component.ModelComponent;
import io.github.core.data.ExtendedModelInstance;
import io.github.core.data.enumsTypes.ModelType;
import io.github.core.game_engine.factory.InstanceFactory;
import io.github.shared.data.component.LifeComponent;
import io.github.shared.data.component.MeleeAttackComponent;
import io.github.shared.data.component.NetComponent;
import io.github.shared.data.component.PositionComponent;
import io.github.shared.data.component.ProjectileAttackComponent;
import io.github.shared.data.component.RangedAttackComponent;
import io.github.shared.data.component.VelocityComponent;

public class GraphicsSyncSystem extends BaseSystem {
    private ComponentMapper<ModelComponent> mm;
    private ComponentMapper<NetComponent> mNet;
    private ComponentMapper<PositionComponent> mPos;
    private ComponentMapper<LifeComponent> mLife;
    private ComponentMapper<VelocityComponent> mVel;
    private ComponentMapper<MeleeAttackComponent> mMelee;
    private ComponentMapper<RangedAttackComponent> mRanged;
    private ComponentMapper<ProjectileAttackComponent> mProjAttack;
    private final Queue<ExtendedModelInstance> sharedRenderQueue;

    public GraphicsSyncSystem(Queue<ExtendedModelInstance> modelInstanceQueue) {
        this.sharedRenderQueue = modelInstanceQueue;
    }

    @Override
    protected void processSystem() {
        // Nouvelle liste temporaire
        List<ExtendedModelInstance> newRenderList = new ArrayList<>();

        // Parcourir toutes les entités
        IntBag entities = world.getAspectSubscriptionManager().get(Aspect.all()).getEntities();

        for (int i = 0; i < entities.size(); i++) {
            int e = entities.get(i);

            ModelComponent mc = mm.get(e);
            NetComponent net = mNet.get(e);
            PositionComponent pos = mPos.get(e);
            LifeComponent life = mLife.get(e);
            VelocityComponent vel = mVel.get(e);
            MeleeAttackComponent melee = mMelee.get(e);
            RangedAttackComponent ranged = mRanged.get(e);
            ProjectileAttackComponent projectile = mProjAttack.get(e);

            if (mc == null) {
                mc = mm.create(e);
                mc.mapInstance = (net != null ? InstanceFactory.getExtendedModelInstance(net.entityType,net.netId,e) : InstanceFactory.getDefaultExtendedModelInstance(e));
            }

            // Mettre à jour instance
            mc.mapInstance.updateEntityInstance(pos,life,vel,melee,ranged,projectile);
            if(net != null) {
                if (melee != null) {
                    mc.mapInstance.updateWeaponInstance(vel,melee.weaponType,melee.currentCooldown, melee.horizontalRotation, melee.verticalRotation);
                }

                if (ranged != null) {
                    mc.mapInstance.updateWeaponInstance(vel,ranged.weaponType, ranged.currentCooldown, ranged.horizontalRotation, ranged.verticalRotation);
                }

                if (projectile != null) {
                    mc.mapInstance.updateWeaponInstance(vel,projectile.weaponType, projectile.currentCooldown, projectile.horizontalRotation, projectile.verticalRotation);
                }
            }


            // Ajouter à la nouvelle liste
            newRenderList.add(mc.mapInstance);
        }

        // Remplacer la liste partagée par la nouvelle
        sharedRenderQueue.clear();
        sharedRenderQueue.addAll(newRenderList);
    }

}

