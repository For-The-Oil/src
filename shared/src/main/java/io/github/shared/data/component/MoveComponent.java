package io.github.shared.data.component;

import com.artemis.Component;
import com.artemis.annotations.PooledWeaver;

@PooledWeaver
public class MoveComponent extends Component {
    public boolean targetRelated;
    public float destinationX;
    public float destinationY;
    public float nextX1;
    public float nextY1;
    public float nextX2;
    public float nextY2;
    public boolean force = false;

    public void reset() {
        this.destinationX = -1;
        this.destinationY = -1;
        this.nextX1 = -1;
        this.nextY1 = -1;
        this.nextX2 = -1;
        this.nextY2 = -1;
        targetRelated = false;
        force = false;
    }

    public void set(boolean targetRelated,float destinationX, float destinationY, float nextX1, float nextY1, float nextX2, float nextY2, boolean force) {
        this.targetRelated = targetRelated;
        this.force = force;
        this.destinationX = destinationX;
        this.destinationY = destinationY;
        this.nextX1 = nextX1;
        this.nextY1 = nextY1;
        this.nextX2 = nextX2;
        this.nextY2 = nextY2;
    }
}
