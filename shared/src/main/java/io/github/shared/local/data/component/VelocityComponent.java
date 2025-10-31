 package io.github.shared.local.data.component;

 import com.artemis.Component;
 import com.artemis.annotations.PooledWeaver;
 import com.badlogic.gdx.math.Vector2;


 @PooledWeaver
 public class VelocityComponent extends Component {
     public float vx;
     public float vy;

     public void reset() {
         vx = 0f;
         vy = 0f;
     }

     public void set(float vx, float vy) {
         this.vx = vx;
         this.vy = vy;
     }

     public boolean isMoving() {
         return vx != 0f || vy != 0f;
     }

     public float getSpeed() {
         return (float) Math.sqrt(vx * vx + vy * vy);
     }
 }

