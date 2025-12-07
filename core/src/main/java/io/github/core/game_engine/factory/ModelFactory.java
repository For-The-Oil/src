package io.github.core.game_engine.factory;


import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.Texture.TextureWrap;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.loader.G3dModelLoader;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder.VertexInfo;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.UBJsonReader;


import net.mgsx.gltf.loaders.glb.GLBLoader;
import net.mgsx.gltf.loaders.gltf.GLTFLoader;
import net.mgsx.gltf.scene3d.scene.SceneAsset;

import java.util.HashMap;
import java.util.Locale;

import io.github.shared.config.BaseGameConfig;
import io.github.shared.data.enums_types.CellType;
import io.github.shared.data.enums_types.EntityType;
import io.github.shared.data.enums_types.WeaponType;

public class ModelFactory {

    private static ModelFactory INSTANCE;
    private static Model defaultModel;
    private static Model defaultModelShape;
    private static HashMap<Object,Model> HashMapModel;
    private static HashMap<Object,String> HashMapPath;

    private static AssetManager am = new AssetManager();
    private ModelFactory(){
        ModelBuilder builder = new ModelBuilder();
        HashMapModel = new HashMap<>();

        for(Object o : HashMapPath.keySet()){
            if(o instanceof CellType) am.load(HashMapPath.get(o),Texture.class);
        }
        am.finishLoading();
        for(Object o : HashMapPath.keySet()){
            if(o instanceof CellType) HashMapModel.put(o,createSolModel(am,HashMapPath.get(o)));
            else HashMapModel.put(o,loadModel(HashMapPath.get(o)));
        }
        am.finishLoading();

        // Crée un modèle par défaut
        defaultModel = builder.createBox(BaseGameConfig.CELL_SIZE*0.75f, BaseGameConfig.CELL_SIZE*0.75f, BaseGameConfig.CELL_SIZE*0.75f,
            new Material(ColorAttribute.createDiffuse(Color.RED)),
            VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal);

        // Crée un modèle par défaut
        defaultModelShape = createDefaultSolModel();


    }

    public static ModelFactory getInstance() {
        if (INSTANCE == null) {
            if(HashMapPath == null){throw new NullPointerException("The HashMapPath is null");}
            INSTANCE = new ModelFactory();
        }
        return INSTANCE;
    }

    public static void initINSTANCE(HashMap<Object,String> MapPath){
        HashMapPath = MapPath;
    }


    public static void disposeINSTANCE(){
        for(Model model : HashMapModel.values()){
            model.dispose();
        }
        HashMapModel.clear();
        defaultModel.dispose();
        defaultModelShape.dispose();
        am.dispose();
        HashMapModel = null;
        defaultModel = null;
        defaultModelShape = null;
        INSTANCE = null;
    }

    public static Model createSolModel(AssetManager am,String pngPath) {
        Texture texture = am.get(pngPath, Texture.class);
        texture.setFilter(TextureFilter.Linear, TextureFilter.Linear);
        texture.setWrap(TextureWrap.ClampToEdge, TextureWrap.ClampToEdge);

        Material material = new Material(TextureAttribute.createDiffuse(texture));

        ModelBuilder mb = new ModelBuilder();
        mb.begin();

        long attrs = VertexAttributes.Usage.Position
            | VertexAttributes.Usage.Normal
            | VertexAttributes.Usage.TextureCoordinates;

        MeshPartBuilder part = mb.part("sol", GL20.GL_TRIANGLES, attrs, material);

        Vector3 up = new Vector3(0f, 1f, 0f);

        VertexInfo v1 = new VertexInfo().setPos(0f,0f,0f).setNor(up).setUV(0f, 0f);
        VertexInfo v2 = new VertexInfo().setPos(0f,0f,BaseGameConfig.CELL_SIZE).setNor(up).setUV(0f, 1f);
        VertexInfo v3 = new VertexInfo().setPos(BaseGameConfig.CELL_SIZE,0f,BaseGameConfig.CELL_SIZE).setNor(up).setUV(1f, 1f);
        VertexInfo v4 = new VertexInfo().setPos(BaseGameConfig.CELL_SIZE,0f,0f).setNor(up).setUV(1f, 0f);

        // Ordre CCW vu de dessus pour une normale (0,1,0)
        part.rect(v1, v2, v3, v4);

        return mb.end();
    }

    public static Model createDefaultSolModel() {
        // Matériau uni magenta (pas de texture)
        Material material = new Material(ColorAttribute.createDiffuse(Color.MAGENTA));

        ModelBuilder mb = new ModelBuilder();
        mb.begin();

        long attrs = VertexAttributes.Usage.Position
            | VertexAttributes.Usage.Normal; // plus de UV

        MeshPartBuilder part = mb.part("sol", GL20.GL_TRIANGLES, attrs, material);

        Vector3 up = new Vector3(0f, 1f, 0f);
        float s = BaseGameConfig.CELL_SIZE;

        // Origine = coin haut-gauche (0,0,0) ; plan jusqu'à (s, 0, s)
        VertexInfo v1 = new VertexInfo().setPos(0f, 0f, 0f).setNor(up);
        VertexInfo v2 = new VertexInfo().setPos(0f, 0f, s ).setNor(up);
        VertexInfo v3 = new VertexInfo().setPos(s , 0f, s ).setNor(up);
        VertexInfo v4 = new VertexInfo().setPos(s , 0f, 0f).setNor(up);

        // Ordre CCW vu de dessus → normale +Y
        part.rect(v1, v2, v3, v4);

        return mb.end();
    }


    /** Charge et retourne le Model libGDX depuis un chemin interne. */
    public static Model loadModel(String path) {
        String p = path.toLowerCase(Locale.ROOT);
        FileHandle fh = Gdx.files.internal(path);

        if (p.endsWith(".g3db")) {
            return new G3dModelLoader(new UBJsonReader()).loadModel(fh);            // libGDX core
        } else if (p.endsWith(".g3dj")) {
            return new G3dModelLoader(new JsonReader()).loadModel(fh);              // libGDX core
        } else if (p.endsWith(".glb")) {
            SceneAsset asset = new GLBLoader().load(fh);                            // gdx-gltf
            return asset.scene.model;                                               // <-- Model libGDX
        } else if (p.endsWith(".gltf")) {
            SceneAsset asset = new GLTFLoader().load(fh);                           // gdx-gltf
            return asset.scene.model;                                               // <-- Model libGDX
        }
        throw new IllegalArgumentException("Format non supporté: " + path);
    }


    public Model getDefaultModel() {
        return defaultModel;
    }

    public Model getModel(EntityType entityType) {
        return HashMapModel.getOrDefault(entityType,defaultModel);
    }

    public Model getModel(WeaponType weaponType) {
        return HashMapModel.getOrDefault(weaponType,defaultModel);
    }

    public Model getModel(CellType cellType) {
        return HashMapModel.getOrDefault(cellType,defaultModelShape);
    }
}
