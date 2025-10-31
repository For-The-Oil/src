package io.github.shared.local.data.component;

import com.artemis.Component;
import com.artemis.annotations.PooledWeaver;

@PooledWeaver
public class TargetComponent extends Component {
    public int TargetId;
    public boolean force = false;
}
