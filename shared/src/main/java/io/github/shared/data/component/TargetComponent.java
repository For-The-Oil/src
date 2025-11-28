package io.github.shared.data.component;

import com.artemis.Component;
import com.artemis.PooledComponent;
import com.artemis.annotations.PooledWeaver;


@PooledWeaver
public class TargetComponent extends PooledComponent {
    public int targetId;
    public int nextTargetId;
    public boolean force = false;

    @Override
    public void reset() {
        targetId = -1;
        nextTargetId = -1;
        force = false;
    }

    public void set(int targetId, int nextTargetId,boolean force) {
        this.targetId = targetId;
        this.nextTargetId = nextTargetId;
        this.force = force;
    }

    public boolean hasTarget() {
        return targetId >= 0;
    }
    public boolean hasNextTarget() {
        return nextTargetId >= 0;
    }
}

