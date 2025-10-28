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

import io.github.shared.local.data.EnumsTypes.KryoMessageType;
import io.github.shared.local.data.EnumsTypes.RequestType;
import io.github.shared.local.data.network.KryoMessage;
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
                    if(object instanceof KryoMessage){
                        KryoMessage kryoMessage = (KryoMessage) object;
                        if (kryoMessage.getObj() instanceof String) {
                            statusMessage = "Got this : " + kryoMessage.getObj();
                            System.out.println(statusMessage);
                        }
                        else if (kryoMessage.getObj() instanceof AuthRequest) {
                            AuthRequest auth = (AuthRequest) kryoMessage.getObj();
                            statusMessage = "Auth response: " + auth.getMode();
                            System.out.println(statusMessage);
                        }
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
            sendLogin("newUser@gmail.com", "pass123");
        }

        // Exemple d’envoi d’une AuthRequest REGISTER avec R
        if (connected && Gdx.input.isKeyJustPressed(Input.Keys.R)) {
            sendRegister("newUser@gmail.com", "pass123", "pass123", "my_username");
        }

        // Exemple d’envoi d’une AuthRequest TOKEN avec T
        if (connected && Gdx.input.isKeyJustPressed(Input.Keys.T)) {
            sendToken("azrsfd-654561-sdfffgx", "561894523454");
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
    private void sendLogin(String email, String password) {
        HashMap<String, String> keys = new HashMap<>();
        keys.put("email", email);
        keys.put("password", password);
        AuthRequest request = new AuthRequest(AuthModeType.LOGIN, keys);
        KryoMessage kryoMessage = new KryoMessage(KryoMessageType.AUTH,null,request);
        client.sendTCP(kryoMessage);
        System.out.println("Login send : " + email);
    }

    private void sendRegister(String email, String password, String password2, String username) {
        HashMap<String, String> keys = new HashMap<>();
        keys.put("email", email);
        keys.put("password", password);
        keys.put("password2", password2);
        keys.put("username", username);
        AuthRequest request = new AuthRequest(AuthModeType.REGISTER, keys);
        KryoMessage kryoMessage = new KryoMessage(KryoMessageType.AUTH,null,request);
        client.sendTCP(kryoMessage);
        System.out.println("Register send : " + email);
    }

    private void sendToken(String uuid, String token){
        HashMap<String, String> keys = new HashMap<>();
        keys.put("UUID", uuid);
        keys.put("token", token);
        AuthRequest request = new AuthRequest(AuthModeType.TOKEN, keys);
        KryoMessage kryoMessage = new KryoMessage(KryoMessageType.AUTH,token,request);
        client.sendTCP(kryoMessage);
        System.out.println("Login send : " + uuid);
    }







}
