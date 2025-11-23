package io.github.shared.data.component;

import com.artemis.Component;
import com.artemis.annotations.PooledWeaver;
@PooledWeaver
public class OnCreationComponent extends Component {
    public int fromNetId;
    public float time;


    public void reset() {
        fromNetId = -1;
        time = -1;
    }

    public void set(int fromNetId,float time) {
        this.fromNetId = fromNetId;
        this.time = time;
    }

}
