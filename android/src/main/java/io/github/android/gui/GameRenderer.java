package io.github.android.gui;


import android.util.Log;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import java.util.ArrayList;

import io.github.core.data.ClientGame;
import io.github.core.data.ExtendedModelInstance;
import io.github.core.game_engine.factory.InstanceFactory;
import io.github.shared.config.BaseGameConfig;


public class GameRenderer implements ApplicationListener {
    private ModelBatch modelBatch;
    private PerspectiveCamera camera;
    private Environment environment;
    private ArrayList<ExtendedModelInstance> entityInstance;
    private ArrayList<ModelInstance> shapeInstance;

    public GameRenderer() {}

    @Override
    public void create() {
        // Batch de rendu 3D
        modelBatch = new ModelBatch();
        entityInstance = new ArrayList<>();
        shapeInstance = new ArrayList<>();

        // Caméra perspective
        camera = new PerspectiveCamera(67f, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        camera.position.set(0f, 150f, (300f+10));
        camera.lookAt(0f, 0f, 0f);
        camera.near = 0.1f;
        camera.far = BaseGameConfig.CELL_SIZE*1000f;
        camera.update();

        // Environnement / éclairage
        environment = new Environment();
        environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.5f, 0.5f, 0.5f, 1f));
        environment.add(new DirectionalLight().set(Color.WHITE, -1f, -0.8f, -0.2f));
    }

    @Override
    public void render() {
        // Nettoyage écran + depth buffer (compat OpenGL ES 2.0)
        Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        Gdx.gl.glClearColor(0f, 0f, 0f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);


        if(!ClientGame.getInstance().getModelInstanceQueue().isEmpty()){
            entityInstance.clear();
            entityInstance.addAll(ClientGame.getInstance().getModelInstanceQueue());
            ClientGame.getInstance().getModelInstanceQueue().clear();
        }
        if(ClientGame.getInstance().isMapDirty()) {
            shapeInstance = InstanceFactory.getShapeInstance(ClientGame.getInstance().getMap(), shapeInstance);
            ClientGame.getInstance().setMapDirty(false);
        }

        // Début/fin du batch
        modelBatch.begin(camera);
        for (int i = 0, n = shapeInstance.size(); i < n; i++) {
            ModelInstance instance = shapeInstance.get(i);
            if (instance != null) {
                modelBatch.render(instance, environment);
            }
        }
        for (int i = 0, n = entityInstance.size(); i < n; i++) {
            ExtendedModelInstance instance = entityInstance.get(i);
            if (instance != null) {
                float delta = Gdx.graphics.getDeltaTime();
                instance.update(delta);
                modelBatch.render(instance, environment);
            }
        }
        modelBatch.end();
    }

    @Override
    public void resize(int width, int height) {
        camera.viewportWidth = width;
        camera.viewportHeight = height;
        camera.update();
    }

    @Override
    public void pause() {}
    @Override
    public void resume() {}

    @Override
    public void dispose() {
        if (modelBatch != null) {
            modelBatch.dispose();
        }
    }

}

