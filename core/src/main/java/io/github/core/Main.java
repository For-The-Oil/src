package io.github.core;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.ScreenUtils;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;

import java.io.IOException;
import java.util.HashMap;

import io.github.shared.local.data.EnumsTypes.RequestType;
import io.github.shared.local.data.network.KryoRegistry;
import io.github.shared.local.data.requests.AuthRequest;
import io.github.shared.local.data.EnumsTypes.AuthModeType;

/**
 * Main LibGDX class avec client KryoNet intégré et AuthRequest support.
 */
public class Main extends ApplicationAdapter {

    private SpriteBatch batch;
    private Texture logo;

    private Client client;
    private boolean connected = false;
    private String statusMessage = "";

    @Override
    public void create() {
        batch = new SpriteBatch();
        logo = new Texture("libgdx.png");

        // === Initialisation du client KryoNet ===
        client = new Client();
        Kryo kryo = client.getKryo();

        // 🔹 Enregistrer toutes les classes nécessaires pour Kryo
        KryoRegistry.registerAll(kryo);

        // Listener KryoNet
        client.addListener(new Listener() {

            @Override
            public void connected(Connection connection) {
                Gdx.app.postRunnable(() -> {
                    connected = true;
                    statusMessage = "connected to the server !";
                    System.out.println(statusMessage);
                });
            }

            @Override
            public void disconnected(Connection connection) {
                Gdx.app.postRunnable(() -> {
                    connected = false;
                    statusMessage = "disconnected from the server.";
                    System.out.println(statusMessage);
                });
            }

            @Override
            public void received(Connection connection, Object object) {
                Gdx.app.postRunnable(() -> {
                    if (object instanceof String) {
                        statusMessage = "Got this : " + object;
                        System.out.println(statusMessage);
                    } else if (object instanceof AuthRequest) {
                        AuthRequest auth = (AuthRequest) object;
                        statusMessage = "Auth response: " + auth.getMode();
                        System.out.println(statusMessage);
                    }
                });
            }
        });

        client.start();

        // Connexion dans un thread séparé
        new Thread(() -> {
            try {
                client.connect(5000, "127.0.0.1", 54555);
            } catch (IOException e) {
                Gdx.app.postRunnable(() -> {
                    statusMessage = "Error connexion : " + e.getMessage();
                    System.err.println(statusMessage);
                });
            }
        }, "KryoNet-ConnectThread").start();
    }

    @Override
    public void render() {
        // Nettoyage de l’écran
        ScreenUtils.clear(0.1f, 0.1f, 0.15f, 1f);

        batch.begin();
        batch.draw(logo, 140, 210);
        batch.end();

        // Envoyer un message simple avec ESPACE
        if (connected && Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            String msg = "Hello server from LibGDX !";
            client.sendTCP(msg);
            System.out.println("Send : " + msg);
        }

        // Exemple d’envoi d’une AuthRequest LOGIN avec L
        if (connected && Gdx.input.isKeyJustPressed(Input.Keys.L)) {
            sendLogin("testUser", "1234");
        }

        // Exemple d’envoi d’une AuthRequest REGISTER avec R
        if (connected && Gdx.input.isKeyJustPressed(Input.Keys.R)) {
            sendRegister("newUser", "pass123");
        }

        // Quitter avec ESCAPE
        if (connected && Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            client.sendTCP("exit");
            client.close();
            connected = false;
        }
    }

    @Override
    public void dispose() {
        batch.dispose();
        logo.dispose();
        if (client != null) client.stop();
    }

    // 🔹 Méthodes utilitaires pour AuthRequest
    private void sendLogin(String username, String password) {
        HashMap<String, String> keys = new HashMap<>();
        keys.put("username", username);
        keys.put("password", password);
        AuthRequest request = new AuthRequest(AuthModeType.LOGIN, keys);
        client.sendTCP(request);
        System.out.println("Login send : " + username);
    }

    private void sendRegister(String username, String password) {
        HashMap<String, String> keys = new HashMap<>();
        keys.put("username", username);
        keys.put("password", password);
        AuthRequest request = new AuthRequest(AuthModeType.REGISTER, keys);
        client.sendTCP(request);
        System.out.println("Register send : " + username);
    }
}
