package io.github.shared.data.component;

import com.artemis.Component;
import com.artemis.PooledComponent;
import com.artemis.annotations.PooledWeaver;

@PooledWeaver
public class MoveComponent extends PooledComponent {
    public boolean targetRelated;
    public float destinationX;
    public float destinationY;
    public boolean force = false;

    @Override
    public void reset() {
        this.destinationX = -1;
        this.destinationY = -1;
        targetRelated = false;
        force = false;
    }

    public void set(boolean targetRelated,float destinationX, float destinationY, boolean force) {
        this.targetRelated = targetRelated;
        this.force = force;
        this.destinationX = destinationX;
        this.destinationY = destinationY;
    }
}
