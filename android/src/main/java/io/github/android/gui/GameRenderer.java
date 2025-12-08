
package io.github.android.gui;


import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Cubemap;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g3d.shaders.DepthShader;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

import io.github.core.game_engine.CameraController;
import io.github.core.game_engine.factory.InstanceFactoryScene;
import io.github.core.game_engine.system.GraphicsSyncSystem;
import io.github.shared.config.BaseGameConfig;
import io.github.core.data.ClientGame;

import net.mgsx.gltf.scene3d.lights.DirectionalLightEx;
import net.mgsx.gltf.scene3d.scene.Scene;
import net.mgsx.gltf.scene3d.scene.SceneManager;
import net.mgsx.gltf.scene3d.scene.SceneSkybox;
import net.mgsx.gltf.scene3d.shaders.PBRDepthShaderProvider;
import net.mgsx.gltf.scene3d.shaders.PBRShaderConfig;
import net.mgsx.gltf.scene3d.shaders.PBRShaderProvider;
import net.mgsx.gltf.scene3d.attributes.PBRCubemapAttribute;
import net.mgsx.gltf.scene3d.attributes.PBRTextureAttribute;
import net.mgsx.gltf.scene3d.utils.IBLBuilder;

public class GameRenderer implements ApplicationListener {

    private SceneManager sceneManager;
    private PerspectiveCamera camera;

    private Cubemap diffuseCubemap, environmentCubemap, specularCubemap;
    private Texture brdfLUT;
    private SceneSkybox skybox;

    private final List<Scene> mapScenes = new ArrayList<>();
    private final List<Scene> entityScenes = new ArrayList<>();

    private Queue<Scene> sharedSceneQueue; // alimentée par l’ECS

    private Runnable onCameraReady;

    @Override
    public void create() {
        // Camera
        camera = new PerspectiveCamera(67f, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        camera.position.set(1500f, 2000f, 800f);
        camera.lookAt(1500f, 0f, 800f);
        //camera.up.set(Vector3.Y);
        //camera.fieldOfView = 90f;
        camera.near = 0.1f;
        camera.far  = BaseGameConfig.CELL_SIZE * 1000f;
        camera.update();

        if(onCameraReady != null) onCameraReady.run();

        // SceneManager PBR (config lights/bones)
        PBRShaderConfig cfg = new PBRShaderConfig();
        cfg.numDirectionalLights = 1; cfg.numPointLights = 0; cfg.numSpotLights = 0; cfg.numBones = 32;

        DepthShader.Config depthCfg = new DepthShader.Config();
        sceneManager = new SceneManager(new PBRShaderProvider(cfg), new PBRDepthShaderProvider(depthCfg));

        sceneManager.setCamera(camera);

        // Lumière + IBL + skybox
        DirectionalLightEx sun = new DirectionalLightEx();
        sun.direction.set(1f, -3f, 1f).nor(); sun.color.set(Color.RED);
        sceneManager.environment.add(sun);

        IBLBuilder ibl = IBLBuilder.createOutdoor(sun);
        environmentCubemap = ibl.buildEnvMap(1024);
        diffuseCubemap = ibl.buildIrradianceMap(256);
        specularCubemap = ibl.buildRadianceMap(10);
        brdfLUT = new Texture(Gdx.files.classpath("net/mgsx/gltf/shaders/brdfLUT.png"));
        ibl.dispose();

        sceneManager.setAmbientLight(0.1f); // augmente la visibilité globale
        sceneManager.environment.set(PBRCubemapAttribute.createSpecularEnv(specularCubemap));
        sceneManager.environment.set(PBRCubemapAttribute.createDiffuseEnv(diffuseCubemap));
        sceneManager.environment.set(new PBRTextureAttribute(PBRTextureAttribute.BRDFLUTTexture, brdfLUT));
        skybox = new SceneSkybox(environmentCubemap);
        sceneManager.setSkyBox(skybox);

        // Queue partagée remplie par l’ECS
        sharedSceneQueue = ClientGame.getInstance().getSceneQueue();

        // Map initiale -> scènes de tuiles (wrap ModelInstance -> Scene)
        mapScenes.clear();
        mapScenes.addAll(InstanceFactoryScene.getShapeScenes(ClientGame.getInstance().getMap(), new ArrayList<>()));
        for (Scene s : mapScenes) if (s != null) sceneManager.addScene(s);
    }


    public Iterable<Scene> getAllScenes() {
        List<Scene> all = new ArrayList<>(mapScenes.size() + entityScenes.size());
        all.addAll(mapScenes);
        all.addAll(entityScenes);
        return all;
    }



    @Override
    public void render() {
        GraphicsSyncSystem gfx = ClientGame.getInstance()
            .getWorld()
            .getSystem(GraphicsSyncSystem.class);
        gfx.syncOnRenderThread();

        Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        Gdx.gl.glClearColor(0f, 0f, 0f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

        CameraController.get().applyToCamera(camera);


        // -----------------------
        // 1) MAP UPDATE
        // -----------------------
        if (ClientGame.getInstance().isMapDirty()) {
            for (Scene s : mapScenes) if (s != null) sceneManager.removeScene(s);
            mapScenes.clear();

            mapScenes.addAll(
                InstanceFactoryScene.getShapeScenes(
                    ClientGame.getInstance().getMap(), new ArrayList<>()
                )
            );
            for (Scene s : mapScenes) if (s != null) sceneManager.addScene(s);
            ClientGame.getInstance().setMapDirty(false);
        }

        // -----------------------
        // 2) ENTITY UPDATE
        // -----------------------
        if (!sharedSceneQueue.isEmpty()) {
            for (Scene s : entityScenes) if (s != null) sceneManager.removeScene(s);
            entityScenes.clear();
            entityScenes.addAll(sharedSceneQueue);
            sharedSceneQueue.clear();
            for (Scene s : entityScenes) if (s != null) sceneManager.addScene(s);
        }

        // -----------------------
        // 3) UPDATE TRANSFORMS
        // -----------------------
        for (Scene s : entityScenes)
            if (s != null && s.modelInstance != null) s.modelInstance.calculateTransforms();

        for (Scene s : mapScenes)
            if (s != null && s.modelInstance != null) s.modelInstance.calculateTransforms();

        // -----------------------
        // 4) RENDER
        // -----------------------
        float delta = Gdx.graphics.getDeltaTime();
        sceneManager.update(delta);
        sceneManager.render();
    }



    @Override public void resize(int w, int h) { camera.viewportWidth = w; camera.viewportHeight = h; camera.update(); }
    @Override public void pause() {}
    @Override public void resume() {}
    @Override
    public void dispose() {
        if (sceneManager != null) sceneManager.dispose();
        if (skybox != null) skybox.dispose();
        if (diffuseCubemap != null) diffuseCubemap.dispose();
        if (environmentCubemap != null) environmentCubemap.dispose();
        if (specularCubemap != null) specularCubemap.dispose();
        if (brdfLUT != null) brdfLUT.dispose();
    }


    public PerspectiveCamera getCamera() {
        return camera;
    }


    public void setOnCameraReady(Runnable callback) {
        this.onCameraReady = callback;
    }
}
