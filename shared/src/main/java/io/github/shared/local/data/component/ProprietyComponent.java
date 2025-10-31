package io.github.shared.local.data.component;

import com.artemis.Component;
import com.artemis.annotations.PooledWeaver;

@PooledWeaver
public class ProprietyComponent extends Component {
    public String player;
    public String team;
}
