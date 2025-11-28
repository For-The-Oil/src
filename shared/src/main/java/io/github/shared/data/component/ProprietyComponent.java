package io.github.shared.data.component;

import com.artemis.Component;
import com.artemis.PooledComponent;
import com.artemis.annotations.PooledWeaver;

import java.util.UUID;


@PooledWeaver
public class ProprietyComponent extends PooledComponent {
    public UUID player;
    public String team;

    @Override
    public void reset() {
        player = null;
        team = null;
    }

    public void set(UUID player, String team) {
        this.player = player;
        this.team = team;
    }
}
