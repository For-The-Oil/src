package io.github.shared.local.data.component;

import com.artemis.Component;
import com.artemis.annotations.PooledWeaver;
@PooledWeaver
public class OnCreationComponent extends Component {
    public int from;
    public float x;
    public float y;
    public long time;


    public void reset() {
        x = y = 0;
        from = 0;
    }

    public void set(int x, int y, int from,long time) {
        this.x = x;
        this.y = y;
        this.from = from;
        this.time = time;
    }

}
