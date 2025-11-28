package io.github.shared.data.component;

import com.artemis.Component;
import com.artemis.PooledComponent;
import com.artemis.annotations.PooledWeaver;


@PooledWeaver
public class FreezeComponent extends PooledComponent {
    public float freeze_time;

    public void setFreeze(float duration) {
        this.freeze_time = duration;
    }

    public boolean isFrozen() {
        return freeze_time > 0f;
    }

    @Override
    public void reset() {
        freeze_time = 0f;
    }
}

