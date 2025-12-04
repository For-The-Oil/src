package io.github.core.game_engine.factory;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;

import io.github.shared.data.enums_types.CellType;
import io.github.shared.data.enums_types.EntityType;
import io.github.shared.data.enums_types.WeaponType;

public class ModelFactory {

    private static ModelFactory INSTANCE;
    private static Model defaultModel;
    private static Model defaultModelShape;
    private ModelFactory(){
        ModelBuilder builder = new ModelBuilder();

        // Crée un modèle par défaut
        defaultModel = builder.createBox(75f, 75f, 75f,
            new Material(ColorAttribute.createDiffuse(Color.RED)),
            VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal);

        // Crée un modèle par défaut
        defaultModelShape = builder.createBox(100f, 1f, 100f,
            new Material(ColorAttribute.createDiffuse(Color.MAGENTA)),
            VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal);

    }

    public static ModelFactory getInstance() {
        if (INSTANCE == null) INSTANCE = new ModelFactory();
        return INSTANCE;
    }
    public Model getDefaultModel() {
        return defaultModel;
    }

    public Model getModel(EntityType entityType) {
        switch (entityType) {
            default:
                return defaultModel;
        }
    }

    public Model getModel(WeaponType weaponType) {
        switch (weaponType) {
            default:
                return defaultModel;
        }
    }

    public Model getModel(CellType cellType) {
        switch (cellType) {
            default:
                return defaultModelShape;
        }
    }
}
