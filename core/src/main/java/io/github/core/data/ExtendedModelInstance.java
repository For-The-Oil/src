package io.github.core.data;

import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.utils.AnimationController;
import com.badlogic.gdx.math.Vector3;

import io.github.shared.data.component.LifeComponent;
import io.github.shared.data.component.MeleeAttackComponent;
import io.github.shared.data.component.PositionComponent;
import io.github.shared.data.component.ProjectileAttackComponent;
import io.github.shared.data.component.RangedAttackComponent;
import io.github.shared.data.component.VelocityComponent;

public class ExtendedModelInstance extends ModelInstance {
    private int entityId;// ID de l'entité ECS
    private int entityNetId;// ID de l'entité ECS
    private float health;// Vie actuelle
    private float maxHealth;// Vie max
    private float  translationX ;
    private float  translationY ;
    private float   translationZ ;
    private boolean isAlive;// État

    private AnimationController animationController;

    public ExtendedModelInstance(Model model, int entityId, float x, float y, float z) {
        super(model);
        this.entityId = entityId;
        this.animationController = new AnimationController(this);
        this.translationX = x;
        this.translationY = y;
        this.translationZ = z;
    }

    public ExtendedModelInstance(Model model,int entityNetId , int entityId, float x, float y, float z) {
        super(model);
        this.entityId = entityId;
        this.entityNetId = entityNetId;
        this.animationController = new AnimationController(this);
        this.translationX = x;
        this.translationY = y;
        this.translationZ = z;
    }

    public void updateFromComponents(PositionComponent pos, LifeComponent life, VelocityComponent velocity, MeleeAttackComponent melee, RangedAttackComponent ranged, ProjectileAttackComponent projectile) {
        // Mettre à jour la transformation
        if(pos != null) {
            this.transform.setToRotation(Vector3.Y, pos.horizontalRotation);
            this.transform.rotate(Vector3.X, pos.verticalRotation);
            this.transform.setTranslation(this.translationX+pos.x, this.translationY+pos.y, this.translationZ+pos.z);
        }

        // Mettre à jour la vie
        if (life != null) {
            this.health = life.health;
            this.maxHealth = life.maxHealth;
            this.isAlive = life.isAlive();
        }


        if (velocity != null) {
            if (velocity.isStop()) {
                if (animationController.current != null && "Move".equals(animationController.current.animation.id)) {
                    animationController.current = null; // Stoppe l'animation
                }
            } else {
                if (animationController.current == null || (!"Move".equals(animationController.current.animation.id)&&!"Attack".equals(animationController.current.animation.id))) {
                    animationController.animate("Move", -1); // Animation Run en boucle
                }
            }
        }


        if (melee != null) {
            if (melee.currentCooldown <= melee.weaponType.getAnimationCooldown()) {
                animationController.setAnimation("Attack", 1);
            }
        }

        if (ranged != null) {
            if (ranged.currentCooldown <= ranged.weaponType.getAnimationCooldown()) {
                animationController.setAnimation("Attack", 1);
            }
        }

        if (projectile != null) {
            if (projectile.currentCooldown <= projectile.weaponType.getAnimationCooldown()) {
                animationController.setAnimation("Attack", 1);
            }
        }

    }

}

