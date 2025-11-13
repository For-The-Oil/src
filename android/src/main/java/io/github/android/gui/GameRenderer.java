package io.github.android.gui;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL30;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Vector3;

public class GameRenderer implements ApplicationListener {

    private ModelBatch modelBatch;
    private Model cubeModel;
    private ModelInstance cubeInstance;
    private PerspectiveCamera camera;
    private Environment environment;

    // Angles de rotation
    private float rotationX = 0f;
    private float rotationY = 0f;
    private final float rotationSpeedX = 30f; // degrés/seconde
    private final float rotationSpeedY = 45f; // degrés/seconde

    public GameRenderer() {}

    @Override
    public void create() {
        modelBatch = new ModelBatch();

        // Création d’un cube avec six couleurs distinctes (chaque face différente)
        ModelBuilder modelBuilder = new ModelBuilder();
        cubeModel = modelBuilder.createBox(
            2f, 2f, 2f,
            new com.badlogic.gdx.graphics.g3d.Material(
                ColorAttribute.createDiffuse(Color.RED),
                ColorAttribute.createSpecular(Color.WHITE)
            ),
            com.badlogic.gdx.graphics.VertexAttributes.Usage.Position |
                com.badlogic.gdx.graphics.VertexAttributes.Usage.Normal
        );
        cubeInstance = new ModelInstance(cubeModel);

        // Caméra perspective
        camera = new PerspectiveCamera(67, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        camera.position.set(5f, 5f, 5f);
        camera.lookAt(0, 0, 0);
        camera.near = 0.1f;
        camera.far = 100f;
        camera.update();

        // Lumière
        environment = new Environment();
        environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.5f, 0.5f, 0.5f, 1f));
        environment.add(new DirectionalLight().set(Color.WHITE, -1f, -0.8f, -0.2f));
    }

    @Override
    public void render() {
        float delta = Gdx.graphics.getDeltaTime();

        // Rotation fluide
        rotationX += rotationSpeedX * delta;
        rotationY += rotationSpeedY * delta;

        // Appliquer la rotation
        cubeInstance.transform.setToRotation(Vector3.X, rotationX)
            .rotate(Vector3.Y, rotationY);

        // Nettoyage écran
        Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL30.GL_COLOR_BUFFER_BIT | GL30.GL_DEPTH_BUFFER_BIT);

        // Rendu 3D
        modelBatch.begin(camera);
        modelBatch.render(cubeInstance, environment);
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
        modelBatch.dispose();
        cubeModel.dispose();
    }
}
