package io.github.android.gui;


import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.World;
import com.artemis.utils.IntBag;
import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Cubemap;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g3d.shaders.DepthShader;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

import io.github.core.data.component.ModelComponent;
import io.github.core.game_engine.CameraController;
import io.github.core.game_engine.factory.InstanceFactoryScene;
import io.github.core.game_engine.system.GraphicsSyncSystem;
import io.github.shared.config.BaseGameConfig;
import io.github.core.data.ClientGame;
import io.github.shared.data.component.LifeComponent;

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
    private Queue<Scene> sharedSceneQueue;

    private Runnable onCameraReady;
    private Runnable onLibGdxReady;

    // HUD
    private SpriteBatch hudBatch;
    private Texture white; // 1x1 blanche

    // Style SC2
    private static final float MAX_BAR_WIDTH      = 500f;
    private static final float BAR_HEIGHT         = 20f;
    private static final float BASE_SQUARE_SIZE   = 40f;
    private static final float MIN_SQUARE_SIZE    = 16f;
    private static final float SPACING            = 8f;
    private static final float BASE_HP_PER_SQUARE = 50f;   // HP for Square
    private static final float HEIGHT_OFFSET      = 0.2f;  // Décalage au-dessus de l’unité
    private static final Color SQUARE_COLOR = new Color(0f,1f,0f,1f);


    @Override
    public void create() {
        camera = new PerspectiveCamera(67f, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        camera.position.set(1500f, 2000f, 800f);
        camera.lookAt(1500f, 0f, 800f);
        camera.near = 0.1f;
        camera.far = BaseGameConfig.CELL_SIZE * 1000f;
        camera.update();
        if (onCameraReady != null) onCameraReady.run();

        PBRShaderConfig cfg = new PBRShaderConfig();
        cfg.numDirectionalLights = 1; cfg.numPointLights = 0; cfg.numSpotLights = 0; cfg.numBones = 32;
        DepthShader.Config depthCfg = new DepthShader.Config();
        sceneManager = new SceneManager(new PBRShaderProvider(cfg), new PBRDepthShaderProvider(depthCfg));
        sceneManager.setCamera(camera);

        DirectionalLightEx sun = new DirectionalLightEx();
        sun.direction.set(1f, -3f, 1f).nor();
        sun.color.set(Color.RED);
        sceneManager.environment.add(sun);

        IBLBuilder ibl = IBLBuilder.createOutdoor(sun);
        environmentCubemap = ibl.buildEnvMap(1024);
        diffuseCubemap = ibl.buildIrradianceMap(256);
        specularCubemap = ibl.buildRadianceMap(10);
        brdfLUT = new Texture(Gdx.files.classpath("net/mgsx/gltf/shaders/brdfLUT.png"));
        ibl.dispose();

        sceneManager.setAmbientLight(0.1f);
        sceneManager.environment.set(PBRCubemapAttribute.createSpecularEnv(specularCubemap));
        sceneManager.environment.set(PBRCubemapAttribute.createDiffuseEnv(diffuseCubemap));
        sceneManager.environment.set(new PBRTextureAttribute(PBRTextureAttribute.BRDFLUTTexture, brdfLUT));

        skybox = new SceneSkybox(environmentCubemap);
        sceneManager.setSkyBox(skybox);

        sharedSceneQueue = ClientGame.getInstance().getSceneQueue();

        mapScenes.clear();
        mapScenes.addAll(InstanceFactoryScene.getShapeScenes(ClientGame.getInstance().getMap(), new ArrayList<>()));
        for (Scene s : mapScenes) if (s != null) sceneManager.addScene(s);

        if (onLibGdxReady != null) onLibGdxReady.run();

        hudBatch = new SpriteBatch();
        Pixmap pm = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pm.setColor(Color.WHITE);
        pm.fill();
        white = new Texture(pm);
        pm.dispose();

        // Filtrage net + wrap
        white.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        white.setWrap(Texture.TextureWrap.ClampToEdge, Texture.TextureWrap.ClampToEdge);
    }

    public Iterable<Scene> getAllScenes() {
        List<Scene> all = new ArrayList<>(mapScenes.size() + entityScenes.size());
        all.addAll(mapScenes);
        all.addAll(entityScenes);
        return all;
    }

    @Override
    public void render() {
        GraphicsSyncSystem gfx = ClientGame.getInstance().getWorld().getSystem(GraphicsSyncSystem.class);
        gfx.syncOnRenderThread();

        Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        Gdx.gl.glClearColor(0f, 0f, 0f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

        CameraController.get().applyToCamera(camera);

        if (ClientGame.getInstance().isMapDirty()) {
            for (Scene s : mapScenes) if (s != null) sceneManager.removeScene(s);
            mapScenes.clear();
            mapScenes.addAll(InstanceFactoryScene.getShapeScenes(
                ClientGame.getInstance().getMap(), new ArrayList<>())
            );
            for (Scene s : mapScenes) if (s != null) sceneManager.addScene(s);
            ClientGame.getInstance().setMapDirty(false);
        }

        if (!sharedSceneQueue.isEmpty()) {
            for (Scene s : entityScenes) if (s != null) sceneManager.removeScene(s);
            entityScenes.clear();
            entityScenes.addAll(sharedSceneQueue);
            sharedSceneQueue.clear();
            for (Scene s : entityScenes) if (s != null) sceneManager.addScene(s);
        }

        for (Scene s : entityScenes)
            if (s != null && s.modelInstance != null) s.modelInstance.calculateTransforms();
        for (Scene s : mapScenes)
            if (s != null && s.modelInstance != null) s.modelInstance.calculateTransforms();

        float delta = Gdx.graphics.getDeltaTime();
        sceneManager.update(delta);
        sceneManager.render();

        drawHealthBarsSC2(gfx);
    }


    private void drawHealthBarsSC2(GraphicsSyncSystem gfx) {
        if (hudBatch == null || white == null) return;

        World world = ClientGame.getInstance().getWorld();
        ComponentMapper<LifeComponent> lifeMapper = world.getMapper(LifeComponent.class);
        ComponentMapper<ModelComponent> modelMapper = world.getMapper(ModelComponent.class);

        hudBatch.begin();
        IntBag entities = world.getAspectSubscriptionManager().get(Aspect.all(LifeComponent.class, ModelComponent.class)).getEntities();
        for (int i = 0; i < entities.size(); i++) {
            int e = entities.get(i);
            Scene s = modelMapper.get(e).scene;
            LifeComponent life = lifeMapper.get(e);
            if (life == null || s == null || s.modelInstance == null) continue;
            float hp    = Math.max(0f, life.health);
            float hpMax = Math.max(1f, life.maxHealth);

            // Position monde
            Vector3 wp = new Vector3();
            s.modelInstance.transform.getTranslation(wp);

            // Calcul de la hauteur graphique via bounding box
            BoundingBox bbox = new com.badlogic.gdx.math.collision.BoundingBox();
            s.modelInstance.calculateBoundingBox(bbox);
            Vector3 min = new Vector3(bbox.min);
            Vector3 max = new Vector3(bbox.max);
            min.mul(s.modelInstance.transform);
            max.mul(s.modelInstance.transform);
            float avgXZ   = (Math.abs(max.x - min.x) + Math.abs(max.z - min.z)) * 0.5f;
            float screenYOffset = Math.min(avgXZ * HEIGHT_OFFSET + 20f,300);

            // Place la barre au-dessus du sommet réel + petite marge
            float padding = 10f;
            wp.y = max.y + padding;

            // Projection écran
            Vector3 sp = camera.project(wp);
            if (sp.z <= 0f || sp.z >= 1f) continue;

            float vw = Gdx.graphics.getWidth();
            float vh = Gdx.graphics.getHeight();
            if (sp.x < -50 || sp.x > vw + 50 || sp.y < -50 || sp.y > vh + 50) continue;

            // Utilise graphicHeight pour ajuster le décalage vertical
            drawSC2Bar(sp.x, sp.y + screenYOffset, hp, hpMax);
        }

        hudBatch.end();
    }


    private void drawSC2Bar(float centerX, float baseY, float hp, float hpMax) {
        int squares = (int)Math.ceil(hpMax / BASE_HP_PER_SQUARE);
        squares = Math.max(squares, 1);

        float squareSize = BASE_SQUARE_SIZE;
        float hpPerSquare = BASE_HP_PER_SQUARE;

        // Si trop large, on réduit le nombre de carrés
        float neededWidth = squares * squareSize + (squares - 1) * SPACING;
        if (neededWidth > MAX_BAR_WIDTH) {
            int maxSquaresFit = (int)Math.floor((MAX_BAR_WIDTH + SPACING) / (MIN_SQUARE_SIZE + SPACING));
            squares = Math.max(maxSquaresFit, 1);
            hpPerSquare = hpMax / squares;
            squareSize = MIN_SQUARE_SIZE; // taille minimale pour tout faire tenir
            neededWidth = squares * squareSize + (squares - 1) * SPACING;
        }
        float xStart = centerX - neededWidth / 2f;

        // Fond noir + bord
        drawRect(xStart - SPACING, baseY - SPACING, neededWidth + 2 * SPACING, BAR_HEIGHT + 2 * SPACING, Color.BLACK);

        // PV remplis
        int full = (int)(hp / hpPerSquare);
        float remainder = hp - full * hpPerSquare;
        float part = hpPerSquare > 0 ? MathUtils.clamp(remainder / hpPerSquare, 0f, 1f) : 0f;

        float x = xStart;
        for (int i = 0; i < squares; i++) {
            if (i < full) {
                drawRect(x, baseY, squareSize, BAR_HEIGHT, SQUARE_COLOR);
            } else if (i == full && part > 0f) {
                drawRect(x, baseY, squareSize * part, BAR_HEIGHT, SQUARE_COLOR);
            }
            x += squareSize + SPACING;
        }
    }


    private void drawRect(float x, float y, float w, float h, Color c) {
        hudBatch.setColor(c);
        hudBatch.draw(white, x, y, w, h);
        hudBatch.setColor(Color.WHITE);
    }

    @Override
    public void resize(int w, int h) {
        camera.viewportWidth = w;
        camera.viewportHeight = h;
        camera.update();
    }

    @Override public void pause() { }
    @Override public void resume() { }

    @Override
    public void dispose() {
        if (sceneManager != null) sceneManager.dispose();
        if (skybox != null) skybox.dispose();
        if (diffuseCubemap != null) diffuseCubemap.dispose();
        if (environmentCubemap != null) environmentCubemap.dispose();
        if (specularCubemap != null) specularCubemap.dispose();
        if (brdfLUT != null) brdfLUT.dispose();
        if (hudBatch != null) hudBatch.dispose();
        if (white != null) white.dispose();
    }

    public PerspectiveCamera getCamera() {
        return camera;
    }

    public void setOnCameraReady(Runnable callback) {
        this.onCameraReady = callback;
    }

    public void setOnLibGdxReady(Runnable r) {
        onLibGdxReady = r;
    }
}
