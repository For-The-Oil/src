package io.github.shared.data.component;

import com.artemis.Component;
import com.artemis.PooledComponent;
import com.artemis.annotations.PooledWeaver;
@PooledWeaver
public class OnCreationComponent extends PooledComponent {
    public int fromNetId;
    public float time;

    @Override
    public void reset() {
        fromNetId = -1;
        time = -1;
    }

    public void set(int fromNetId,float time) {
        this.fromNetId = fromNetId;
        this.time = time;
    }

}
