package io.github.shared.local.data.component;

import com.artemis.Component;
import com.artemis.annotations.PooledWeaver;


@PooledWeaver
public class ProprietyComponent extends Component {
    public String player;
    public String team;

    public void reset() {
        player = null;
        team = null;
    }

    public void set(String player, String team) {
        this.player = player;
        this.team = team;
    }
}
