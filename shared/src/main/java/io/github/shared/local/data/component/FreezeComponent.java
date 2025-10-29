package io.github.shared.local.data.component;

import com.artemis.Component;
import com.artemis.annotations.PooledWeaver;

@PooledWeaver
public class FreezeComponent extends Component {
    public float freeze_time = 0f;
}
