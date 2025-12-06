package io.github.core.data;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.model.Animation;
import com.badlogic.gdx.graphics.g3d.model.Node;
import com.badlogic.gdx.graphics.g3d.utils.AnimationController;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;

import io.github.shared.data.component.LifeComponent;
import io.github.shared.data.component.MeleeAttackComponent;
import io.github.shared.data.component.PositionComponent;
import io.github.shared.data.component.ProjectileAttackComponent;
import io.github.shared.data.component.RangedAttackComponent;
import io.github.shared.data.component.VelocityComponent;
import io.github.shared.data.enums_types.WeaponType;

public class ExtendedModelInstance extends ModelInstance {
    private int entityId;// ID de l'entité ECS
    private int entityNetId;// ID de l'entité ECS
    private float health;// Vie actuelle
    private float maxHealth;// Vie max
    private boolean isAlive;// État

    private AnimationController animationControllerEntity;
    private AnimationController animationControllerMelee;
    private AnimationController animationControllerRange;
    private AnimationController animationControllerProjectileLauncher;

    public ExtendedModelInstance(Model model, int entityId) {
        super(model);
        this.entityId = entityId;
        this.animationControllerEntity = new AnimationController(this);
        this.animationControllerMelee = new AnimationController(this);
        this.animationControllerRange = new AnimationController(this);
        this.animationControllerProjectileLauncher = new AnimationController(this);
    }

    public ExtendedModelInstance(Model model,int entityNetId , int entityId) {
        super(model);
        this.entityId = entityId;
        this.entityNetId = entityNetId;
        this.animationControllerEntity = new AnimationController(this);
        this.animationControllerMelee = new AnimationController(this);
        this.animationControllerRange = new AnimationController(this);
        this.animationControllerProjectileLauncher = new AnimationController(this);
    }

    public void updateEntityInstance(PositionComponent pos, LifeComponent life, VelocityComponent velocity, MeleeAttackComponent melee, RangedAttackComponent ranged, ProjectileAttackComponent projectile) {
        // Mettre à jour la transformation
        if(pos != null) {
            Matrix4 finalTransform = new Matrix4().idt()//X et y inversé pour libgdx
                .translate(pos.x, pos.z, pos.y)
                .rotate(Vector3.Z, pos.horizontalRotation)
                .rotate(Vector3.X, pos.verticalRotation);
            this.transform.set(finalTransform);
        }

        // Mettre à jour la vie
        if (life != null) {
            this.health = life.health;
            this.maxHealth = life.maxHealth;
            this.isAlive = life.isAlive();
        }

        if (velocity != null) {
            if (velocity.isStop()) {
                if (animationControllerEntity.current != null && "Move".equals(animationControllerEntity.current.animation.id)) {
                    animationControllerEntity.current.loopCount = 0; // Stoppe l'animation
                }
            } else {
                if (animationControllerEntity.current == null || (!"Move".equals(animationControllerEntity.current.animation.id)&&!"Attack".equals(animationControllerEntity.current.animation.id))) {
                    if(hasAnimation("Move")){
                        animationControllerEntity.animate("Move", -1);// Animation Run en boucle
                    }
                }
            }
        }
        if(hasAnimation("Attack")){
            if (melee != null && melee.currentCooldown <= melee.weaponType.getAnimationCooldown()) {
                animationControllerEntity.animate("Attack", 1,1f, null, 0);
            }
            if (ranged != null && ranged.currentCooldown <= ranged.weaponType.getAnimationCooldown()) {
                animationControllerEntity.animate("Attack", 1,1f, null, 0);
            }
            if (projectile != null && projectile.currentCooldown <= projectile.weaponType.getAnimationCooldown()) {
                animationControllerEntity.animate("Attack", 1,1f, null, 0);
            }
        }
        this.calculateTransforms();
    }

    public void updateWeaponInstance(VelocityComponent velocity,WeaponType weaponType,float currentCooldown, float secondHorizontalRotation, float secondVerticalRotation) {
        String name ="";
        AnimationController animationController = null;
        if (weaponType.getType().equals(WeaponType.Type.Melee)){
            name = "Melee";
            animationController = animationControllerMelee;
            Node turretNode = this.getNode("Melee");
            if(weaponType.isTurret()) {
                turretNode.rotation.set(new Quaternion(Vector3.Z, secondHorizontalRotation));
                turretNode.rotation.set(new Quaternion(Vector3.X, secondVerticalRotation));
            }
        }
        else if (weaponType.getType().equals(WeaponType.Type.Range)) {
            name = "Range";
            animationController = animationControllerRange;
            Node turretNode = this.getNode("Range");
            if(weaponType.isTurret()) {
                turretNode.rotation.set(new Quaternion(Vector3.Z, secondHorizontalRotation));
                turretNode.rotation.set(new Quaternion(Vector3.X, secondVerticalRotation));
            }
        }
        else if (weaponType.getType().equals(WeaponType.Type.ProjectileLauncher)) {
            name = "ProjectileLauncher";
            animationController = animationControllerProjectileLauncher;
            Node turretNode = this.getNode("ProjectileLauncher");
            if(weaponType.isTurret()) {
                turretNode.rotation.set(new Quaternion(Vector3.Z, secondHorizontalRotation));
                turretNode.rotation.set(new Quaternion(Vector3.X, secondVerticalRotation));
            }
        }

        if (velocity != null && !name.isEmpty()&&animationController!=null) {
            if (velocity.isStop()) {
                if (animationController.current != null && ("Move"+name).equals(animationController.current.animation.id)) {
                    animationController.current.loopCount = 0; // Stoppe l'animation
                }
            } else {
                if (animationController.current == null || (!("Move"+name).equals(animationController.current.animation.id)&&! ("Attack"+name).equals(animationController.current.animation.id))) {
                    if(hasAnimation(("Move"+name))){
                        animationController.animate(("Move"+name), -1,1f, null, 0);// Animation Run en boucle
                    }
                }
            }
        }

        if (weaponType != null && hasAnimation("Attack") && currentCooldown <= weaponType.getAnimationCooldown()) {
        animationController.animate("Attack",1,1f, null, 0);
        }
        this.calculateTransforms();
    }


    private boolean hasAnimation(String animId) {
        if (this.animations == null) return false;
        for (Animation a : this.animations) {
            if (a != null && a.id != null && a.id.equals(animId)) return true;
        }
        return false;
    }


    public boolean isAlive() {
        return isAlive;
    }

    public float getMaxHealth() {
        return maxHealth;
    }

    public float getHealth() {
        return health;
    }

    public int getEntityNetId() {
        return entityNetId;
    }

    public int getEntityId() {
        return entityId;
    }

    public void update(float delta) {
        animationControllerEntity.update(delta);
        animationControllerMelee.update(delta);
        animationControllerRange.update(delta);
        animationControllerProjectileLauncher.update(delta);
    }
}

