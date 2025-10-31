package io.github.shared.local.data.component;

import com.artemis.Component;
import com.artemis.annotations.PooledWeaver;

import java.util.HashMap;

import io.github.shared.local.data.EnumsTypes.RessourcesType;

@PooledWeaver
public class RessourceComponent extends Component {
    private HashMap<RessourcesType, Integer> ressources = new HashMap<>();
}
