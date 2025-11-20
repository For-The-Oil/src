package io.github.shared.data.component;

import com.artemis.Component;
import com.artemis.annotations.PooledWeaver;

import java.util.UUID;


@PooledWeaver
public class ProprietyComponent extends Component {
    public UUID player;
    public String team;

    public void reset() {
        player = null;
        team = null;
    }

    public void set(UUID player, String team) {
        this.player = player;
        this.team = team;
    }
}
