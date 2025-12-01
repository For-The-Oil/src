package io.github.core.game_engine.system;


import com.artemis.Aspect;
import com.artemis.BaseSystem;
import com.artemis.ComponentMapper;
import com.artemis.utils.IntBag;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import io.github.core.data.component.ModelComponent;
import io.github.core.data.ExtendedModelInstance;
import io.github.core.data.enumsTypes.ModelType;
import io.github.core.data.ClientGame;
import io.github.shared.data.enumsTypes.EntityType;
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

    public GraphicsSyncSystem(ClientGame game) {
        this.sharedRenderQueue = game.getModelInstanceQueue();
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
                mc = new ModelComponent(net != null ? createModelInstance(net.entityType,net.netId,e) : createDefaultModelInstance(e));
                world.edit(e).add(mc);
            }

            // Mettre à jour instance
            mc.mapInstance.get(ModelType.Entity).updateFromComponents(pos,life,vel,null,null,null);

            if(melee != null){
                if(mc.mapInstance.get(ModelType.Melee) == null) {
                    mc.mapInstance.put(ModelType.Melee, net != null ? createModelInstance(net.entityType, net.netId, e) : createDefaultModelInstance(e));
                }
                mc.mapInstance.get(ModelType.Melee).updateFromComponents(pos,life,vel,melee,null,null);
            }

            if(ranged != null){
                if(mc.mapInstance.get(ModelType.Range) == null) {
                    mc.mapInstance.put(ModelType.Range, net != null ? createModelInstance(net.entityType, net.netId, e) : createDefaultModelInstance(e));
                }
                mc.mapInstance.get(ModelType.Range).updateFromComponents(pos,life,vel,null,ranged,null);
            }

            if(projectile != null){
                if(mc.mapInstance.get(ModelType.ProjectileLauncher) == null) {
                    mc.mapInstance.put(ModelType.ProjectileLauncher, net != null ? createModelInstance(net.entityType, net.netId, e) : createDefaultModelInstance(e));
                }
                mc.mapInstance.get(ModelType.ProjectileLauncher).updateFromComponents(pos,life,vel,null,null,projectile);
            }


            // Ajouter à la nouvelle liste
            newRenderList.addAll(mc.mapInstance.values());
        }

        // Remplacer la liste partagée par la nouvelle
        sharedRenderQueue.clear();
        sharedRenderQueue.addAll(newRenderList);
    }

    private ExtendedModelInstance createModelInstance(EntityType entityType, int net, int e) {
        //return new ExtendedModelInstance(,net,e);///TODO
        return createDefaultModelInstance(e);
    }

    private ExtendedModelInstance createDefaultModelInstance(int e) {
        // Crée un modèle par défaut (cube rouge par exemple)
        ModelBuilder builder = new ModelBuilder();
        Model model = builder.createBox(1f, 1f, 1f,
            new Material(ColorAttribute.createDiffuse(Color.RED)),
            VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal);
        return new ExtendedModelInstance(model,e);
    }
}

