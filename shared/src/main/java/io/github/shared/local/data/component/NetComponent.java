package io.github.shared.local.data.component;

import com.artemis.Component;
import com.artemis.annotations.PooledWeaver;

@PooledWeaver
public class NetComponent extends Component {

    // Il faut voir si un Int sera suffisant pour notre jeu, peut être passer à long au besoin.
    public int netId;

}
