package io.github.shared.local.data.component;

import com.artemis.Component;
import com.artemis.annotations.PooledWeaver;
import com.artemis.utils.Bag;

@PooledWeaver
public class DamageComponent extends Component {
    public final Bag<DamageEntry> entries = new Bag<>();

    // Ajouter une attaque
    public void addDamage(DamageEntry entry) {
        entries.add(entry);
    }

    // Réinitialiser le composant après traitement
    public void clear() {
        entries.clear();
    }
}
