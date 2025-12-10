package io.github.core.game_engine.factory;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;

import net.mgsx.gltf.loaders.glb.GLBLoader;
import net.mgsx.gltf.loaders.gltf.GLTFLoader;
import net.mgsx.gltf.scene3d.scene.Scene;
import net.mgsx.gltf.scene3d.scene.SceneAsset;

import java.util.HashMap;
import java.util.Map;

import io.github.shared.data.enums_types.EntityType;
import io.github.shared.data.enums_types.CellType;

import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;


public class SceneFactory {

    private static SceneFactory INSTANCE;
    private static Map<Object, String> PATHS = new HashMap<>(); // évite NPE
    private static Map<Object, SceneAsset> cache = new HashMap<>();

    private SceneFactory(){
        cache = new HashMap<>();
        for(Object o : PATHS.keySet()){
            cache.put(o,loadAsset(PATHS.get(o)));
        }
    }

    public static SceneFactory getInstance() {
        if (INSTANCE == null) {
            if(PATHS == null){throw new NullPointerException("The HashMapPath is null");}
            INSTANCE = new SceneFactory();
        }
        return INSTANCE;
    }

    public static void initINSTANCE(Map<Object,String> map){
        PATHS = map;
    }

    public static void disposeINSTANCE(){
        if(cache != null) {
            for (SceneAsset model : cache.values()) {
                model.dispose();
            }
        }
        cache = null;
        INSTANCE = null;
    }

    private SceneAsset loadAsset(String path){
        FileHandle fh = Gdx.files.internal(path);
        if (path.endsWith(".glb"))  return new GLBLoader().load(fh);
        if (path.endsWith(".gltf")) return new GLTFLoader().load(fh);
        throw new IllegalArgumentException("Expected .glb/.gltf: " + path);
    }

    // Entités glTF
    public Scene getEntityScene(EntityType type){
        SceneAsset a = cache.get(type);
        if(a == null)return getDefaultEntityScene();
        return new Scene(a.scene);
    }

    public Scene getDefaultEntityScene(){
        ModelInstance mi = new ModelInstance(ModelFactory.getInstance().getDefaultModel());
        return new Scene(mi);
    }

    public Scene getCellScene(CellType cell){
        ModelInstance mi = new ModelInstance(ModelFactory.getInstance().getModel(cell));
        return new Scene(mi);
    }

}


