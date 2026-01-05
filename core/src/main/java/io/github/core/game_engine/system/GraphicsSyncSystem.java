
package io.github.core.game_engine.system;

import com.artemis.Aspect;
import com.artemis.BaseSystem;
import com.artemis.ComponentMapper;
import com.artemis.utils.IntBag;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.BlendingAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.IntAttribute;
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
import io.github.shared.data.component.FreezeComponent;
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
import io.github.shared.data.enums_types.Direction;
import io.github.shared.data.enums_types.EntityType;
import io.github.shared.shared_engine.Utility;

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
    private ComponentMapper<FreezeComponent> mFreeze;

    private final Queue<Scene> sharedRenderQueue;

    public GraphicsSyncSystem(Queue<Scene> sceneQueue) {
        this.sharedRenderQueue = sceneQueue;
    }

    /** No-op: on appelle explicitement syncOnRenderThread() sur le thread rendu. */
    @Override
    protected void processSystem() {
        // no-op
    }
    public int getFrom(EntityType entityType) {
        EntityType type = entityType.getFrom();
        if(type == null)return -1;
        IntBag entities = world.getAspectSubscriptionManager().get(Aspect.all(NetComponent.class)).getEntities();
        for (int i = 0; i < entities.size(); i++) {
            int e = entities.get(i);
            NetComponent net  = mNet.get(e);
            if (net.entityType == type) {
                return net.netId;
            }
        }
        return -1;
    }


    public int getEntity(Scene scene) {
        if(scene == null)return -1;
        IntBag entities = world.getAspectSubscriptionManager().get(Aspect.all(ModelComponent.class)).getEntities();
        for (int i = 0; i < entities.size(); i++) {
            int e = entities.get(i);
            ModelComponent mc = mm.get(e);
            if (mc.scene == scene) {
                return e;
            }
        }
        return -1;
    }

    //TODO : simplifier la logique des fonctions get, en utilisant l'EnumType DeckCardCategory

    /**
     * Renvoit la liste des batiments de type industry appartenant au joueur
     * @return ArrayList<Integer>
     */
    public ArrayList<Integer> getEntityBuildingIndustry() {
        ArrayList<Integer> arrayList = new ArrayList<>();
        IntBag entities = world.getAspectSubscriptionManager().get(Aspect.all(NetComponent.class,BuildingMapPositionComponent.class,RessourceComponent.class)).getEntities();
        for (int i = 0; i < entities.size(); i++) {
            int e = entities.get(i);
            ProprietyComponent meP = mProp.get(e);
            if(meP.player.equals(SessionManager.getInstance().getUuidClient()))arrayList.add(e);
        }
        return arrayList;
    }

    /**
     * Renvoit la liste des entités de type militaire appartenant au joueur
     * @return ArrayList<Integer>
     */
    public ArrayList<Integer> getEntityBuildingMilitary() {
        ArrayList<Integer> arrayList = new ArrayList<>();
        IntBag entities = world.getAspectSubscriptionManager().get(Aspect.all(NetComponent.class, BuildingMapPositionComponent.class)).getEntities();
        for (int i = 0; i < entities.size(); i++) {
            int e = entities.get(i);
            NetComponent net  = mNet.get(e);
            ProprietyComponent meP = mProp.get(e);
            if(!meP.player.equals(SessionManager.getInstance().getUuidClient()))continue;
            for (EntityType entityType : EntityType.values()){
                if(entityType.getFrom() == net.entityType){
                    arrayList.add(e);
                    break;
                }
            }
        }
        return arrayList;
    }

    /**
     *
     * @return ArrayList<Integer>
     */
    public ArrayList<Integer> getEntityBuildingDefense() {
        ArrayList<Integer> arrayList = new ArrayList<>();
        IntBag entities = world.getAspectSubscriptionManager().get(Aspect.all(NetComponent.class, BuildingMapPositionComponent.class)).getEntities();
        for (int i = 0; i < entities.size(); i++) {
            int e = entities.get(i);
            ProprietyComponent meP = mProp.get(e);
            if(meP.player.equals(SessionManager.getInstance().getUuidClient())&&
                (mMelee.get(e) != null || mRanged.get(e) != null || mProjAttack.get(e) != null))
                arrayList.add(e);
        }
        return arrayList;
    }

    /**
     *
     * @return ArrayList<Integer>
     */
    public ArrayList<Integer> getEntityUnit() {
        ArrayList<Integer> arrayList = new ArrayList<>();
        IntBag entities = world.getAspectSubscriptionManager().get(Aspect.all(NetComponent.class)).getEntities();
        for (int i = 0; i < entities.size(); i++) {
            int e = entities.get(i);
            NetComponent net  = mNet.get(e);
            ProprietyComponent meP = mProp.get(e);
            if(meP.player.equals(SessionManager.getInstance().getUuidClient())&&
                net.entityType.getType() == EntityType.Type.Unit)arrayList.add(e);
        }
        return arrayList;
    }

    public int getEntityNetID(Scene scene) {
        int e = getEntity(scene);
        if(e!=-1){
            NetComponent net  = mNet.get(e);
            if(net!=null && net.netId>=0){
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
            FreezeComponent fc = mFreeze.get(e);
            OnCreationComponent occ = mOnCreation.get(e);

            if(occ != null && net!=null && !(net.entityType.getType() == EntityType.Type.Building))continue;

            // Création de la Scene (.glb)
            if (mc == null) {
                mc = mm.create(e);
                mc.scene = (net != null)
                    ? SceneFactory.getInstance().getEntityScene(net.entityType)
                    : SceneFactory.getInstance().getDefaultEntityScene();
                if (mc.scene == null) continue; // type non mappé
                if(net!=null && bp != null && pos != null && (net.entityType.getType() == EntityType.Type.Building)){// ton angle en radians
                    mc.scene.modelInstance.transform.translate(
                        pos.x + (Utility.cellToWorld(net.entityType.getShapeType().getShape().getWidth())/2),
                        pos.z,
                        pos.y + (Utility.cellToWorld(net.entityType.getShapeType().getShape().getHeight())/2));
                    mc.scene.modelInstance.transform.rotate(Vector3.Y, (-bp.direction.getAngleRadians()-(float)Math.PI/2) * MathUtils.radiansToDegrees);
                    mc.scene.modelInstance.calculateTransforms();
                }
            }

            Scene s = mc.scene;

            // Transform (X/Z/Y comme dans ton code historique)
            if (pos != null) {
                if(net==null || !(net.entityType.getType() == EntityType.Type.Building)) {
                    Matrix4 t = new Matrix4().idt()
                        .translate(pos.x, pos.z, pos.y)
                        .rotate(Vector3.Y, pos.horizontalRotation * MathUtils.radiansToDegrees)
                        .rotate(Vector3.X, pos.verticalRotation * MathUtils.radiansToDegrees);
                    s.modelInstance.transform.set(t);
                }
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

            if (fc != null && net != null && occ == null) {
                float alpha = MathUtils.clamp(fc.freeze_time / net.entityType.getFreeze_time(), 0f, 1f);
                Scene outline = createBlueOutlineScene(s, 1.00f, alpha,Color.BLUE);
                if (outline != null) newRender.add(outline);
            }
            else if (occ != null && net != null) {
                float alpha = MathUtils.clamp(occ.time / net.entityType.getCreate_time(), 0f, 1f);
                Scene outline = createBlueOutlineScene(s, 1.00f, alpha,Color.WHITE);
                if (outline != null) newRender.add(outline);
            }

            newRender.add(s);
        }

        // Remplace le contenu de la queue partagée (consommée ensuite par GameRenderer)
        sharedRenderQueue.clear();
        sharedRenderQueue.addAll(newRender);
    }

    // --- Utilitaires ---
    private static synchronized Scene createBlueOutlineScene(Scene base, float scale, float alpha, Color color) {
        if (base == null || base.modelInstance == null) return null;

        // Clone + transform identique
        ModelInstance mi = new ModelInstance(base.modelInstance.model);
        mi.transform.set(base.modelInstance.transform);

        // Épaissir : scale léger
        Matrix4 t = new Matrix4(mi.transform);
        t.scale(scale, scale, scale);
        mi.transform.set(t);

        // Matériaux outline : bleu émissif + semi-transparent + culling désactivé
        for (Material mat : mi.materials) {
            mat.clear();

            // Couleur (émissif) pour rester lisible, l’alpha est géré par le blending
            mat.set(ColorAttribute.createEmissive(color));

            // Blending (alpha 0..1) — half transparent
            mat.set(new BlendingAttribute(true, Math.max(0f, Math.min(1f, alpha))));

            // Rendre les deux côtés (évite la disparition sur faces horizontales)
            mat.set(new IntAttribute(IntAttribute.CullFace, GL20.GL_NONE));

            // Optionnel : test Z ON, écriture Z OFF (si ton shader lit l’attribut)
            // mat.set(new DepthTestAttribute(GL20.GL_LEQUAL, false));
        }

        Scene outline = new Scene(mi);
        outline.modelInstance.calculateTransforms();
        return outline;
    }

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
