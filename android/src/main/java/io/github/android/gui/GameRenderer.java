
package io.github.android.gui;

import android.util.Log;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Cubemap;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g3d.RenderableProvider;
import com.badlogic.gdx.graphics.g3d.shaders.DepthShader;
import com.badlogic.gdx.utils.Array;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

import io.github.core.game_engine.factory.InstanceFactoryScene;
import io.github.core.game_engine.factory.SceneFactory;
import io.github.core.game_engine.system.GraphicsSyncSystem;
import io.github.shared.config.BaseGameConfig;
import io.github.core.data.ClientGame; // suppose un getter de queue Scenes (voir patch plus bas)
import io.github.shared.data.enums_types.EntityType;

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

    @Override
    public void create() {
        // Camera
        camera = new PerspectiveCamera(67f, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        camera.position.set(1500, 150 * 20f, 300f * 30f);
        camera.lookAt(1500, 0f, 0f);
        camera.near = 0.1f;
        camera.far  = BaseGameConfig.CELL_SIZE * 1000f;
        camera.update();

        // SceneManager PBR (config lights/bones)
        PBRShaderConfig cfg = new PBRShaderConfig();
        cfg.numDirectionalLights = 1; cfg.numPointLights = 0; cfg.numSpotLights = 0; cfg.numBones = 32;

        DepthShader.Config depthCfg = new DepthShader.Config();
        sceneManager = new SceneManager(new PBRShaderProvider(cfg), new PBRDepthShaderProvider(depthCfg));

        sceneManager.setCamera(camera);

        // Lumière + IBL + skybox
        DirectionalLightEx sun = new DirectionalLightEx();
        sun.direction.set(1f, -3f, 1f).nor(); sun.color.set(Color.WHITE);
        sceneManager.environment.add(sun);

        IBLBuilder ibl = IBLBuilder.createOutdoor(sun);
        environmentCubemap = ibl.buildEnvMap(1024);
        diffuseCubemap = ibl.buildIrradianceMap(256);
        specularCubemap = ibl.buildRadianceMap(10);
        brdfLUT = new Texture(Gdx.files.classpath("net/mgsx/gltf/shaders/brdfLUT.png"));
        ibl.dispose();

        //sceneManager.setAmbientLight(1f); // augmente la visibilité globale
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


    @Override
    public void render() {
        GraphicsSyncSystem gfx = ClientGame.getInstance().getWorld().getSystem(GraphicsSyncSystem.class);
        gfx.syncOnRenderThread();

        Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        Gdx.gl.glClearColor(0f, 0f, 0f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

        // Map dirty -> régénère les scènes de tuiles
        if (ClientGame.getInstance().isMapDirty()) {
            for (Scene s : mapScenes) if (s != null) sceneManager.removeScene(s);
            mapScenes.clear();
            mapScenes.addAll(InstanceFactoryScene.getShapeScenes(ClientGame.getInstance().getMap(), new ArrayList<>()));
            for (Scene s : mapScenes) if (s != null) sceneManager.addScene(s);
            ClientGame.getInstance().setMapDirty(false);
        }

        // Ingestion de la queue ECS -> entités visibles (ex. si tu veux rafraîchir la liste à l’écran)
        if (!sharedSceneQueue.isEmpty()) {
            entityScenes.clear();
            entityScenes.addAll(sharedSceneQueue);
            sharedSceneQueue.clear();
        }

        Array<RenderableProvider> providers = sceneManager.getRenderableProviders();
        providers.clear();
        for (Scene s : mapScenes) providers.add(s);
        for (Scene s : entityScenes) providers.add(s);

        for (Scene s : entityScenes){
            if (s != null && s.modelInstance != null) {
                s.modelInstance.calculateTransforms();  // <-- safety net
            }
        }



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
}
