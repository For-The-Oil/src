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

/** LibGDX main class that also connects to a KryoNet server and exchanges Strings. */
public class Main extends ApplicationAdapter {
    private SpriteBatch batch;
    private Texture image;

    private Client client;
    private boolean connected = false;

    private String lastMessage = "";

    @Override
    public void create() {
        batch = new SpriteBatch();
        image = new Texture("libgdx.png");

        // === Démarrage du client réseau ===
        client = new Client();
        Kryo kryo = client.getKryo();
        kryo.register(String.class); // important : même enregistrement que côté serveur

        // Ajout d’un listener pour gérer les événements réseau
        client.addListener(new Listener() {
            @Override
            public void connected(Connection connection) {
                Gdx.app.postRunnable(() -> {
                    connected = true;
                    lastMessage = "Connecté au serveur !";
                    System.out.println("✅ Connecté au serveur !");
                });
            }

            @Override
            public void disconnected(Connection connection) {
                Gdx.app.postRunnable(() -> {
                    connected = false;
                    lastMessage = "Déconnecté du serveur.";
                    System.out.println("❌ Déconnecté du serveur.");
                });
            }

            @Override
            public void received(Connection connection, Object object) {
                if (object instanceof String) {
                    String msg = (String) object;
                    Gdx.app.postRunnable(() -> {
                        lastMessage = msg;
                        System.out.println("📩 Reçu du serveur: " + msg);
                    });
                }
            }
        });

        client.start();
        new Thread(() -> {
            try {
                // connexion au serveur local (localhost:54555)
                client.connect(5000, "127.0.0.1", 54555);
            } catch (IOException e) {
                Gdx.app.postRunnable(() -> {
                    lastMessage = "Erreur connexion : " + e.getMessage();
                    System.err.println("Erreur lors de la connexion : " + e.getMessage());
                });
            }
        }, "KryoNet-ConnectThread").start();
    }

    @Override
    public void render() {
        ScreenUtils.clear(0.15f, 0.15f, 0.2f, 1f);
        batch.begin();
        batch.draw(image, 140, 210);
        batch.end();

        // Exemple : appuie sur ESPACE pour envoyer un message au serveur
        if (connected && Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            String msg = "Hello serveur depuis le client LibGDX !";
            client.sendTCP(msg);
            System.out.println("📤 Envoyé : " + msg);
        }

        // Appuie sur ÉCHAP pour envoyer "exit" et fermer
        if (connected && Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            client.sendTCP("exit");
            client.close();
            connected = false;
        }
    }

    @Override
    public void dispose() {
        batch.dispose();
        image.dispose();
        if (client != null) {
            client.stop();
        }
    }
}
