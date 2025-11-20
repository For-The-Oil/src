package io.github.shared.data.component;

import com.artemis.Component;
import com.artemis.annotations.PooledWeaver;

import java.util.HashMap;

import io.github.shared.data.EnumsTypes.RessourcesType;


@PooledWeaver
public class RessourceComponent extends Component {
    private HashMap<RessourcesType, Integer> ressources = new HashMap<>();

    public void reset() {
        ressources.clear();
    }

    public void add(RessourcesType type, int amount) {
        ressources.put(type, ressources.getOrDefault(type, 0) + amount);
    }

    public boolean has(RessourcesType type, int required) {
        return ressources.getOrDefault(type, 0) >= required;
    }

    public int get(RessourcesType type) {
        return ressources.getOrDefault(type, 0);
    }

    public void consume(RessourcesType type, int amount) {
        ressources.put(type, Math.max(ressources.getOrDefault(type, 0) - amount, 0));
    }

    public HashMap<RessourcesType, Integer> getAll() {
        return ressources;
    }
}

