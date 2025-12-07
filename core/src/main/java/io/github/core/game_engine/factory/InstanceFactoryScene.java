
package io.github.core.game_engine.factory;

import net.mgsx.gltf.scene3d.scene.Scene;

import java.util.ArrayList;

import io.github.shared.data.gameobject.Shape;
import io.github.shared.data.gameobject.Cell;
import io.github.shared.data.enums_types.CellType;
import io.github.shared.shared_engine.Utility;


public class InstanceFactoryScene {

    public static ArrayList<Scene> getShapeScenes(Shape shape, ArrayList<Scene> out){
        out.clear();
        if (shape == null || shape.getWidth() == 0 || shape.getHeight() == 0) return out;
        final int w = shape.getWidth(), h = shape.getHeight();
        for (int y=0; y<h; y++){
            for (int x=0; x<w; x++){
                Cell c = shape.getCells(x, y);
                CellType t = (c == null) ? CellType.VOID : c.getCellType();
                if (t == CellType.VOID) continue; // pas de null
                Scene s = SceneFactory.getInstance().getCellScene(t);
                s.modelInstance.transform.setToTranslation(Utility.cellToWorld(x), 0f, Utility.cellToWorld(y));
                out.add(s);
            }
        }
        return out;
    }
}


