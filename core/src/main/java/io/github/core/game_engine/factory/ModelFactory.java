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
import net.mgsx.gltf.scene3d.attributes.PBRColorAttribute;
import net.mgsx.gltf.scene3d.attributes.PBRFloatAttribute;
import net.mgsx.gltf.scene3d.attributes.PBRTextureAttribute;
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
        }

        // Crée un modèle par défaut
        defaultModel = builder.createBox(BaseGameConfig.CELL_SIZE*0.75f, BaseGameConfig.CELL_SIZE*0.75f, BaseGameConfig.CELL_SIZE*0.75f,
            new Material(PBRColorAttribute.createBaseColorFactor(Color.RED)),
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

    public static Model createSolModel(AssetManager am, String pngPath) {
        Texture texture = am.get(pngPath, Texture.class);
        texture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        texture.setWrap(Texture.TextureWrap.ClampToEdge, Texture.TextureWrap.ClampToEdge);

        // Matériau PBR :
        Material material = new Material(
            // texture de base (albedo)
            new PBRTextureAttribute(PBRTextureAttribute.BaseColorTexture, texture),
            // couleur de base (linéaire) — garde WHITE si tu ne veux pas teinter
            PBRColorAttribute.createBaseColorFactor(Color.WHITE),
            // facteurs métal/rough (workflow metallic-roughness glTF)
            PBRFloatAttribute.createMetallic(0f),
            PBRFloatAttribute.createRoughness(1f)
        );

        ModelBuilder mb = new ModelBuilder();
        mb.begin();
        long attrs = VertexAttributes.Usage.Position
            | VertexAttributes.Usage.Normal
            | VertexAttributes.Usage.TextureCoordinates;
        MeshPartBuilder part = mb.part("sol", GL20.GL_TRIANGLES, attrs, material);

        Vector3 up = new Vector3(0f, 1f, 0f);
        float s = BaseGameConfig.CELL_SIZE;
        VertexInfo v1 = new VertexInfo().setPos(0f, 0f, 0f).setNor(up).setUV(0f, 0f);
        VertexInfo v2 = new VertexInfo().setPos(0f, 0f, s ).setNor(up).setUV(0f, 1f);
        VertexInfo v3 = new VertexInfo().setPos(s , 0f, s ).setNor(up).setUV(1f, 1f);
        VertexInfo v4 = new VertexInfo().setPos(s , 0f, 0f).setNor(up).setUV(1f, 0f);
        part.rect(v1, v2, v3, v4);

        return mb.end();
    }

    public static Model createDefaultSolModel() {
        // Matériau uni magenta (pas de texture)
        Material material = new Material(PBRColorAttribute.createBaseColorFactor(Color.MAGENTA));

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

    public Model getDefaultModel() {
        return defaultModel;
    }

    public Model getModel(CellType cellType) {
        return HashMapModel.getOrDefault(cellType,defaultModelShape);
    }
}
