package io.github.shared.local.data.component;

import com.artemis.Component;
import com.artemis.annotations.PooledWeaver;


@PooledWeaver
public class TargetComponent extends Component {
    public int TargetId;
    public boolean force = false;

    public void reset() {
        TargetId = -1;
        force = false;
    }

    public void set(int targetId, boolean force) {
        this.TargetId = targetId;
        this.force = force;
    }

    public boolean hasTarget() {
        return TargetId >= 0;
    }
}

