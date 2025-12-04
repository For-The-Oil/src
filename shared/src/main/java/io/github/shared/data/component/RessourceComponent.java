package io.github.shared.data.component;

import com.artemis.PooledComponent;
import com.artemis.annotations.PooledWeaver;

import java.util.HashMap;

import io.github.shared.data.enums_types.ResourcesType;


@PooledWeaver
public class RessourceComponent extends PooledComponent {
    private HashMap<ResourcesType, Integer> resources = new HashMap<>();

    @Override
    public void reset() {
        resources.clear();
    }

    public void set(HashMap<ResourcesType, Integer> resources) {
        this.resources = resources;
    }

    public void add(ResourcesType type, int amount) {
        resources.put(type, resources.getOrDefault(type, 0) + amount);
    }

    public boolean has(ResourcesType type, int required) {
        return resources.getOrDefault(type, 0) >= required;
    }

    public int get(ResourcesType type) {
        return resources.getOrDefault(type, 0);
    }

    public void consume(ResourcesType type, int amount) {
        resources.put(type, Math.max(resources.getOrDefault(type, 0) - amount, 0));
    }

    public HashMap<ResourcesType, Integer> getAll() {
        return resources;
    }

    public HashMap<ResourcesType, Integer> getResources() {
        return resources;
    }

}

