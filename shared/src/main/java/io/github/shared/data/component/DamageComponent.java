package io.github.shared.data.component;

import com.artemis.Component;
import com.artemis.PooledComponent;
import com.artemis.annotations.PooledWeaver;

import java.util.ArrayList;

import io.github.shared.data.gameobject.DamageEntry;


@PooledWeaver
public class DamageComponent extends PooledComponent {
    public ArrayList<DamageEntry> entries = new ArrayList<>();

    // Ajouter une attaque
    public void addDamage(DamageEntry entry) {
        entries.add(entry);
    }

    // Réinitialiser manuellement (ex. après traitement)
    public void clear() {
        entries.clear();
    }

    // Appelée automatiquement par Artemis si pooling activé
    @Override
    public void reset() {
        entries.clear();
    }

    // Vérifie s'il y a des dégâts à traiter
    public boolean hasDamage() {
        return !entries.isEmpty();
    }

    // Retourne le nombre d'attaques enregistrées
    public int getCount() {
        return entries.size();
    }
}

