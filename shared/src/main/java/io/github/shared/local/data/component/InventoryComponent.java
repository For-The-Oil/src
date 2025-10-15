package io.github.shared.local.data.component;

import java.util.HashMap;
import java.util.Map;

/**
 * Composant représentant l'inventaire d'une unité ou d'un bâtiment.
 * Chaque ressource est associée à une quantité.
 */
public class InventoryComponent {

    private final Map<String, Integer> resources;

    public InventoryComponent() {
        this.resources = new HashMap<>();
    }

    /** Ajoute une quantité à une ressource existante ou crée la ressource si elle n'existe pas */
    public void addResource(String resourceName, int amount) {
        resources.merge(resourceName, amount, Integer::sum);
    }

    /** Retire une quantité d'une ressource, retourne false si pas assez */
    public boolean removeResource(String resourceName, int amount) {
        Integer current = resources.get(resourceName);
        if (current == null || current < amount) return false;
        if (current == amount) resources.remove(resourceName);
        else resources.put(resourceName, current - amount);
        return true;
    }

    /** Récupère la quantité d'une ressource */
    public int getResourceAmount(String resourceName) {
        return resources.getOrDefault(resourceName, 0);
    }

    /** Retourne une copie de la map entière pour lecture seule */
    public Map<String, Integer> getResources() {
        return new HashMap<>(resources);
    }

    /** Vérifie si l'inventaire est vide */
    public boolean isEmpty() {
        return resources.isEmpty();
    }

    /** Vide l'inventaire */
    public void clear() {
        resources.clear();
    }
}
