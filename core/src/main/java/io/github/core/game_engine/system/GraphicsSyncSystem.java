
package io.github.core.game_engine.system;

import com.artemis.Aspect;
import com.artemis.BaseSystem;
import com.artemis.ComponentMapper;
import com.artemis.utils.IntBag;
import com.badlogic.gdx.graphics.g3d.model.Animation;
import com.badlogic.gdx.graphics.g3d.model.Node;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

import io.github.core.client_engine.manager.SessionManager;
import io.github.core.data.component.ModelComponent;
import io.github.core.game_engine.factory.SceneFactory;
import io.github.shared.data.component.BuildingMapPositionComponent;
import io.github.shared.data.component.LifeComponent;
import io.github.shared.data.component.MeleeAttackComponent;
import io.github.shared.data.component.NetComponent;
import io.github.shared.data.component.OnCreationComponent;
import io.github.shared.data.component.PositionComponent;
import io.github.shared.data.component.ProjectileAttackComponent;
import io.github.shared.data.component.ProprietyComponent;
import io.github.shared.data.component.RangedAttackComponent;
import io.github.shared.data.component.RessourceComponent;
import io.github.shared.data.component.VelocityComponent;
import io.github.shared.data.enums_types.EntityType;

import net.mgsx.gltf.scene3d.scene.Scene;

/**
 * Système graphique : ne fait rien dans processSystem() (pour éviter les appels sur un autre thread).
 * Appeler explicitement syncOnRenderThread() depuis GameRenderer.render() (sur le GLThread).
 */
public class GraphicsSyncSystem extends BaseSystem {

    private ComponentMapper<ModelComponent> mm;
    private ComponentMapper<NetComponent> mNet;
    private ComponentMapper<PositionComponent> mPos;
    private ComponentMapper<LifeComponent> mLife;
    private ComponentMapper<VelocityComponent> mVel;
    private ComponentMapper<MeleeAttackComponent> mMelee;
    private ComponentMapper<RangedAttackComponent> mRanged;
    private ComponentMapper<ProjectileAttackComponent> mProjAttack;
    private ComponentMapper<BuildingMapPositionComponent> bPos;
    private ComponentMapper<ProprietyComponent> mProp;
    private ComponentMapper<OnCreationComponent> mOnCreation;

    private final Queue<Scene> sharedRenderQueue;

    public GraphicsSyncSystem(Queue<Scene> sceneQueue) {
        this.sharedRenderQueue = sceneQueue;
    }

    /** No-op: on appelle explicitement syncOnRenderThread() sur le thread rendu. */
    @Override
    protected void processSystem() {
        // no-op
    }

    public int getEntity(Scene scene) {
        if(scene == null)return -1;
        IntBag entities = world.getAspectSubscriptionManager().get(Aspect.all()).getEntities();
        for (int i = 0; i < entities.size(); i++) {
            int e = entities.get(i);
            ModelComponent mc = mm.get(e);
            if (mc != null && mc.scene == scene) {
                return e;
            }
        }
        return -1;
    }
    public ArrayList<Integer> getEntityBuildingIndustry() {
        ArrayList<Integer> arrayList = new ArrayList<>();
        IntBag entities = world.getAspectSubscriptionManager().get(Aspect.all(NetComponent.class,BuildingMapPositionComponent.class,RessourceComponent.class)).getEntities();
        for (int i = 0; i < entities.size(); i++) {
            int e = entities.get(i);
            ProprietyComponent meP = mProp.get(e);
            if(meP.player != SessionManager.getInstance().getUuidClient())continue;
            arrayList.add(e);
        }
        return arrayList;
    }

    public ArrayList<Integer> getEntityBuildingMilitary() {
        ArrayList<Integer> arrayList = new ArrayList<>();
        IntBag entities = world.getAspectSubscriptionManager().get(Aspect.all(NetComponent.class, BuildingMapPositionComponent.class)).getEntities();
        for (int i = 0; i < entities.size(); i++) {
            int e = entities.get(i);
            NetComponent net  = mNet.get(e);
            ProprietyComponent meP = mProp.get(e);
            if(meP.player != SessionManager.getInstance().getUuidClient())continue;
            for (EntityType entityType : EntityType.values()){
                if(entityType.getFrom().equals(net.entityType)){
                    arrayList.add(e);
                    break;
                }
            }
        }
        return arrayList;
    }

    public ArrayList<Integer> getEntityBuildingDefense() {
        ArrayList<Integer> arrayList = new ArrayList<>();
        IntBag entities = world.getAspectSubscriptionManager().get(Aspect.all(NetComponent.class, BuildingMapPositionComponent.class)).getEntities();
        for (int i = 0; i < entities.size(); i++) {
            int e = entities.get(i);
            ProprietyComponent meP = mProp.get(e);
            if(meP.player != SessionManager.getInstance().getUuidClient())continue;
            if(mMelee.get(e) != null || mRanged.get(e) != null || mProjAttack.get(e) != null)arrayList.add(e);
        }
        return arrayList;
    }

    public ArrayList<Integer> getEntityUnit() {
        ArrayList<Integer> arrayList = new ArrayList<>();
        IntBag entities = world.getAspectSubscriptionManager().get(Aspect.all(NetComponent.class)).getEntities();
        for (int i = 0; i < entities.size(); i++) {
            int e = entities.get(i);
            NetComponent net  = mNet.get(e);
            ProprietyComponent meP = mProp.get(e);
            if(meP.player != SessionManager.getInstance().getUuidClient())continue;
            if(net != null && net.entityType.getType().equals(EntityType.Type.Unit))arrayList.add(e);
        }
        return arrayList;
    }

    public int getEntityNetID(Scene scene) {
        int e = getEntity(scene);
        if(e!=-1){
            NetComponent net  = mNet.get(e);
            if(net!=null&&net.netId>0){
            return net.netId;
            }
        }
        return -1;
    }

    /** À appeler depuis GameRenderer.render() sur le GLThread, avant update()/render(). */
    public void syncOnRenderThread() {
        List<Scene> newRender = new ArrayList<>();

        // Récupère toutes les entités ; boucles indexées (pas d’itérateurs imbriqués)
        IntBag entities = world.getAspectSubscriptionManager().get(Aspect.all()).getEntities();
        for (int i = 0; i < entities.size(); i++) {
            int e = entities.get(i);

            ModelComponent mc = mm.get(e);
            NetComponent net  = mNet.get(e);
            PositionComponent pos = mPos.get(e);
            LifeComponent life = mLife.get(e);
            VelocityComponent vel = mVel.get(e);
            MeleeAttackComponent melee = mMelee.get(e);
            RangedAttackComponent ranged = mRanged.get(e);
            ProjectileAttackComponent proj = mProjAttack.get(e);
            BuildingMapPositionComponent bp = bPos.get(e);

            if(mOnCreation.get(e) != null && net!=null && !net.entityType.getType().equals(EntityType.Type.Building))continue;

            // Création de la Scene (.glb)
            if (mc == null) {
                mc = mm.create(e);
                mc.scene = (net != null)
                    ? SceneFactory.getInstance().getEntityScene(net.entityType)
                    : SceneFactory.getInstance().getDefaultEntityScene();
                if (mc.scene == null) continue; // type non mappé
                if(net!=null &&net.entityType.getType().equals(EntityType.Type.Building)){// ton angle en radians
                    mc.scene.modelInstance.transform.rotate(Vector3.Y, bp.direction.getAngleRadians() * MathUtils.radiansToDegrees);
                    mc.scene.modelInstance.calculateTransforms();
                }
            }

            Scene s = mc.scene;

            // Transform (X/Z/Y comme dans ton code historique)
            if (pos != null) {
                Matrix4 t = new Matrix4().idt()
                    .translate(pos.x, pos.z, pos.y)
                    .rotate(Vector3.Y, pos.horizontalRotation* MathUtils.radiansToDegrees)
                    .rotate(Vector3.X, pos.verticalRotation* MathUtils.radiansToDegrees);
                s.modelInstance.transform.set(t);
            }

            // Animations glTF
            if (s.animationController != null) {
                boolean moving = vel != null && !vel.isStop();
                if (moving && hasAnim(s, "Move")) {
                    if (s.animationController.current == null || !"Move".equals(s.animationController.current.animation.id)) {
                        s.animationController.setAnimation("Move", -1);
                    }
                } else if (s.animationController.current != null
                    && "Move".equals(s.animationController.current.animation.id)) {
                    s.animationController.current.loopCount = 0;
                }

                boolean doAttack =
                    (melee != null && melee.currentCooldown <= melee.weaponType.getAnimationCooldown()) ||
                        (ranged != null && ranged.currentCooldown <= ranged.weaponType.getAnimationCooldown()) ||
                        (proj != null && proj.currentCooldown <= proj.weaponType.getAnimationCooldown());
                if (doAttack && hasAnim(s, "Attack") && !"Attack".equals(s.animationController.current.animation.id)) {
                    s.animationController.animate("Attack", 1, 1f, null, 0);
                }
            }
            // Rotation des nœuds d’armes (si tourelle)
            if (melee  != null) rotateNode(s, "Melee",              melee.horizontalRotation,  melee.verticalRotation,  melee.weaponType.isTurret(),melee.weaponType.getTurn_speed(), world.delta);
            if (ranged != null) rotateNode(s, "Range",              ranged.horizontalRotation, ranged.verticalRotation, ranged.weaponType.isTurret(),ranged.weaponType.getTurn_speed(), world.delta);
            if (proj   != null) rotateNode(s, "ProjectileLauncher", proj.horizontalRotation,   proj.verticalRotation,   proj.weaponType.isTurret(),proj.weaponType.getTurn_speed(), world.delta);

            // Recalcul des transforms pour un culling correct
            s.modelInstance.calculateTransforms();

            newRender.add(s);
        }

        // Remplace le contenu de la queue partagée (consommée ensuite par GameRenderer)
        sharedRenderQueue.clear();
        sharedRenderQueue.addAll(newRender);
    }

    // --- Utilitaires ---

    private static boolean hasAnim(Scene s, String id){
        if (s == null || s.modelInstance == null) return false;
        final Array<Animation> anims = s.modelInstance.animations;
        if (anims == null || anims.size == 0) return false;
        for (int i = 0, n = anims.size; i < n; i++){
            final Animation a = anims.get(i);
            if (a != null && id != null && id.equals(a.id)) return true;
        }
        return false;
    }

    private static void rotateNode(Scene s, String nodeName, float horizontalRotation, float verticalRotation, boolean isTurret, float turn_speed, float delta) {
        if (!isTurret || s == null || s.modelInstance == null) return;

        final Node node = s.modelInstance.getNode(nodeName, true);
        if (node == null || turn_speed <= 0f || delta <= 0f) return;

        // Cible : yaw (Y) puis pitch (X) — angles absolus demandés
        final Quaternion target = new Quaternion(Vector3.Y, horizontalRotation)
            .mul(new Quaternion(Vector3.X, verticalRotation))
            .nor();

        // État actuel
        final Quaternion current = node.rotation.nor();

        // Distance angulaire actuelle (degrés)
        float dot = MathUtils.clamp(current.dot(target), -1f, 1f);
        if (dot < 0f) { // plus court chemin
            // inverser la cible conserve la même rotation et évite un chemin plus long
            target.mul(-1f);
            dot = -dot;
        }
        float angleRad = (float)(2.0 * Math.acos(dot));
        float angleDeg = angleRad * MathUtils.radiansToDegrees;

        // Pas max autorisé cette frame (°)
        float stepDeg = turn_speed * delta;

        // Anti-overshoot : si le pas >= écart, on pose la cible
        if (stepDeg >= angleDeg) {
            current.set(target);
        } else {
            float alpha = stepDeg / angleDeg; // 0..1
            current.slerp(target, alpha).nor();
        }

        s.modelInstance.calculateTransforms();
    }
}
