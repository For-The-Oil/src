 package io.github.shared.data.component;

 import com.artemis.Component;
 import com.artemis.annotations.PooledWeaver;
 import com.badlogic.gdx.math.Vector2;


 @PooledWeaver
 public class VelocityComponent extends Component {
     public float vx;
     public float vy;
     public float vz;

     public void reset() {
         vx = 0f;
         vy = 0f;
         vz = 0f;
     }

     public void set(float vx, float vy, float vz) {
         this.vx = vx;
         this.vy = vy;
         this.vz = vz;
     }

     public boolean isStop(){
         return vx == 0 || vy == 0 || vz == 0;
     }

 }

