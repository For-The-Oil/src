
package io.github.core.game_engine.factory;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.attributes.BlendingAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.IntAttribute;

import net.mgsx.gltf.scene3d.scene.Scene;

import java.util.ArrayList;

import io.github.shared.data.enums_types.ShapeType;
import io.github.shared.data.gameobject.Shape;
import io.github.shared.data.gameobject.Cell;
import io.github.shared.data.enums_types.CellType;
import io.github.shared.shared_engine.Utility;


public class InstanceFactoryScene {

    public static ArrayList<Scene> getShapeScenes(Shape shape){
        ArrayList<Scene> out = new ArrayList<>();
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

    public static ArrayList<Scene> pinShapeScenes(Shape shape, float X, float Y, float Z, ShapeType type, Shape map){
        ArrayList<Scene> out = new ArrayList<>();
        if (shape == null || shape.getWidth() == 0 || shape.getHeight() == 0 || X < 0 || Y < 0 || Z < 0) return out;
        final int w = shape.getWidth(), h = shape.getHeight();
        for (int y=0; y<h; y++){
            for (int x=0; x<w; x++){
                Cell c = shape.getCells(x, y);
                CellType t = (c == null) ? CellType.VOID : c.getCellType();
                if (t == CellType.VOID) continue; // pas de null

                int cx = Utility.worldToCell(Utility.cellToWorld(x)+X);
                int cy = Utility.worldToCell(Utility.cellToWorld(y)+Z);
                if(map.getWidth()-1 < cx || map.getHeight()-1 < cy)return new ArrayList<>();

                Scene s = SceneFactory.getInstance().getCellScene(t);
                s.modelInstance.transform.setToTranslation(Utility.cellToWorld(x)+X, 0f+Y, Utility.cellToWorld(y)+Z);
                for (Material mat : s.modelInstance.materials) {
                    mat.clear();

                    // Couleur (émissif) pour rester lisible, l’alpha est géré par le blending
                    if(type.getCanBePlacedOn().contains(map.getCells(cx,cy).getCellType()))mat.set(ColorAttribute.createEmissive(Color.WHITE));
                    else mat.set(ColorAttribute.createEmissive(Color.RED));

                    mat.set(new BlendingAttribute(true, Math.max(0f, Math.min(1f, 0.7f))));

                    // Rendre les deux côtés (évite la disparition sur faces horizontales)
                    mat.set(new IntAttribute(IntAttribute.CullFace, GL20.GL_NONE));
                }
                out.add(s);
            }
        }
        return out;
    }
}


