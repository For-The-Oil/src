package io.github.core.game_engine.factory;

import com.badlogic.gdx.graphics.g3d.*;

import java.util.*;

import io.github.core.data.ExtendedModelInstance;
import io.github.shared.data.enums_types.EntityType;
import io.github.shared.data.enums_types.WeaponType;
import io.github.shared.data.gameobject.Shape;
import io.github.shared.data.gameobject.Cell;
import io.github.shared.data.enums_types.CellType;
import io.github.shared.shared_engine.Utility;

public class InstanceFactory {
    public static ArrayList<ModelInstance> getShapeInstance(Shape shape, ArrayList<ModelInstance> instances) {
        if (shape == null || shape.getWidth() == 0 || shape.getHeight() == 0) return instances;

        final int w = shape.getWidth();
        final int h = shape.getHeight();

        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                final int index = y * w + x;

                // Assure la capacité avant tout accès
                while (instances.size() <= index) instances.add(null);

                Cell c = shape.getCells(x, y);
                CellType t = (c == null) ? CellType.VOID : c.getCellType();

                if (t == CellType.VOID) {
                    // S'il y avait une instance ici, on la supprime
                    if (instances.get(index) != null) instances.set(index, null);
                    continue;
                }

                Model newModel = ModelFactory.getInstance().getModel(t);
                ModelInstance existing = instances.get(index);

                // Si une instance existe déjà avec le même modèle, on réutilise et on met à jour la position
                if (existing != null && existing.model == newModel) {
                    existing.transform.setToTranslation(Utility.cellToWorld(x), 0f, Utility.cellToWorld(y));//X et y inversé pour libgdx
                    continue;
                }

                // Sinon on (re)crée
                ModelInstance inst = new ModelInstance(newModel);
                inst.transform.setToTranslation(Utility.cellToWorld(x), 0f, Utility.cellToWorld(y));//X et y inversé pour libgdx
                instances.set(index, inst);
            }
        }
        return instances;
    }

    public static ExtendedModelInstance getExtendedModelInstance(WeaponType weaponType, int net, int e) {
        return new ExtendedModelInstance(ModelFactory.getInstance().getModel(weaponType),net,e);
    }

    public static ExtendedModelInstance getExtendedModelInstance(EntityType entityType, int net, int e) {
        return new ExtendedModelInstance(ModelFactory.getInstance().getModel(entityType),net,e);
    }

    public static ExtendedModelInstance getDefaultExtendedModelInstance(int e) {
        return new ExtendedModelInstance(ModelFactory.getInstance().getDefaultModel(),e);
    }

    public ModelInstance getDefaultModelInstance() {
        return new ModelInstance(ModelFactory.getInstance().getDefaultModel());
    }
}

