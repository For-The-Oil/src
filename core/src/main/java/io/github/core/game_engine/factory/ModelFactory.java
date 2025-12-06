package io.github.core.game_engine.factory;


import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.Texture.TextureWrap;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder.VertexInfo;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.math.Vector3;


import java.util.HashMap;

import io.github.shared.data.enums_types.CellType;
import io.github.shared.data.enums_types.EntityType;
import io.github.shared.data.enums_types.WeaponType;

public class ModelFactory {

    private static ModelFactory INSTANCE;
    private static Model defaultModel;
    private static Model defaultModelShape;
    private static HashMap<Object,Model> HashMapModel;

    private static AssetManager am = new AssetManager();
    private ModelFactory(){
        ModelBuilder builder = new ModelBuilder();
        HashMapModel = new HashMap<>();
        am.load("",Texture.class);
        am.finishLoading();

        // Crée un modèle par défaut
        defaultModel = builder.createBox(75f, 75f, 75f,
            new Material(ColorAttribute.createDiffuse(Color.RED)),
            VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal);

        // Crée un modèle par défaut
        defaultModelShape = builder.createBox(100f, 1f, 100f,
            new Material(ColorAttribute.createDiffuse(Color.MAGENTA)),
            VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal);

        
    }

    public static void disposeINSTANCE(){
        for(Model model : HashMapModel.values()){
            model.dispose();
        }
        HashMapModel.clear();
        defaultModel.dispose();
        defaultModelShape.dispose();
        am.dispose();
        INSTANCE = null;
    }

    public static Model createSolModel(AssetManager am,String pngPath, float sizeX, float sizeZ) {
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
        VertexInfo v2 = new VertexInfo().setPos(0f,0f,sizeZ).setNor(up).setUV(0f, 1f);
        VertexInfo v3 = new VertexInfo().setPos(sizeX,0f,sizeZ).setNor(up).setUV(1f, 1f);
        VertexInfo v4 = new VertexInfo().setPos(sizeX,0f,0f).setNor(up).setUV(1f, 0f);

        // Ordre CCW vu de dessus pour une normale (0,1,0)
        part.rect(v1, v2, v3, v4);

        return mb.end();
    }
    public static ModelFactory getInstance() {
        if (INSTANCE == null) INSTANCE = new ModelFactory();
        return INSTANCE;
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
