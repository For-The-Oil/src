 package io.github.shared.local.data.component;

 import com.artemis.Component;
 import com.artemis.annotations.PooledWeaver;
 import com.badlogic.gdx.math.Vector2;

 @PooledWeaver
 public class VelocityComponent extends Component {
     public float vx;
     public float vy;
}
