package io.github.core;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;

import java.io.IOException;
import java.util.HashMap;

import io.github.shared.data.EnumsTypes.AuthModeType;
import io.github.shared.data.EnumsTypes.KryoMessageType;
import io.github.shared.data.network.KryoMessage;
import io.github.shared.data.network.KryoRegistry;
import io.github.shared.data.requests.AuthRequest;

public class Main extends ApplicationAdapter {

    private Stage stage;
    private Skin skin;

    private TextField emailField, passwordField, password2Field, usernameField;
    private Label statusLabel;
    private Client client;
    private boolean connected = false;

    @Override
    public void create() {
        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);
        skin = new Skin(Gdx.files.internal("uiskin.json"));

        // UI Elements
        Table table = new Table();
        table.setFillParent(true);
        stage.addActor(table);

        emailField = new TextField("newUser@gmail.com", skin);
        passwordField = new TextField("pass123", skin);
        passwordField.setPasswordMode(true);
        passwordField.setPasswordCharacter('*');
        password2Field = new TextField("", skin);
        password2Field.setPasswordMode(true);
        password2Field.setPasswordCharacter('*');
        usernameField = new TextField("", skin);

        statusLabel = new Label("Not connected", skin);

        TextButton loginButton = new TextButton("Login", skin);
        TextButton registerButton = new TextButton("Register", skin);
        TextButton reconnectButton = new TextButton("Reconnect", skin);

        // Layout
        table.add(new Label("Email:", skin)).left();
        table.add(emailField).width(250).row();
        table.add(new Label("Password:", skin)).left();
        table.add(passwordField).width(250).row();
        table.add(new Label("Password Confirm:", skin)).left();
        table.add(password2Field).width(250).row();
        table.add(new Label("Username:", skin)).left();
        table.add(usernameField).width(250).row();
        table.add(loginButton).padTop(10);
        table.add(registerButton).padTop(10).row();
        table.add(reconnectButton).colspan(2).padTop(10).row();
        table.add(statusLabel).colspan(2).padTop(10).row();

        // KryoNet client init
        client = new Client();
        KryoRegistry.registerAll(client.getKryo());
        client.addListener(new Listener() {
            @Override
            public void connected(Connection connection) {
                Gdx.app.postRunnable(() -> {
                    connected = true;
                    statusLabel.setText("Connected to server!");
                });
            }

            @Override
            public void disconnected(Connection connection) {
                Gdx.app.postRunnable(() -> {
                    connected = false;
                    statusLabel.setText("Disconnected from server.");
                });
            }

            @Override
            public void received(Connection connection, Object object) {
                Gdx.app.postRunnable(() -> {
                    if (object instanceof KryoMessage) {
                        KryoMessage msg = (KryoMessage) object;
                        statusLabel.setText("Received: " + msg.getObj());
                    }
                });
            }
        });

        client.start();
        connectToServer();

        // Button listeners
        loginButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, com.badlogic.gdx.scenes.scene2d.Actor actor) {
                if (connected) sendLogin(emailField.getText(), passwordField.getText());
            }
        });

        registerButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, com.badlogic.gdx.scenes.scene2d.Actor actor) {
                if (connected) sendRegister(emailField.getText(), passwordField.getText(), password2Field.getText(), usernameField.getText());
            }
        });

        reconnectButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, com.badlogic.gdx.scenes.scene2d.Actor actor) {
                if (!connected) {
                    System.out.println("Reconnecting to the server ...");
                    reconnectToServer();
                }
            }
        });
    }

    private void connectToServer() {
        new Thread(() -> {
            try {
                client.connect(5000, "127.0.0.1", 54555);
                System.out.print("Connected to the server !");
            } catch (IOException e) {
                Gdx.app.postRunnable(() -> statusLabel.setText("Connection failed: " + e.getMessage()));
            }
        }, "KryoNet-ConnectThread").start();
    }

    private void reconnectToServer() {
        if (client != null) {
            client.stop();
            client = new Client();
            KryoRegistry.registerAll(client.getKryo());
            client.addListener(new Listener() {
                @Override
                public void connected(Connection connection) {
                    Gdx.app.postRunnable(() -> {
                        connected = true;
                        statusLabel.setText("Reconnected!");
                    });
                }

                @Override
                public void disconnected(Connection connection) {
                    Gdx.app.postRunnable(() -> {
                        connected = false;
                        statusLabel.setText("Disconnected.");
                    });
                }

                @Override
                public void received(Connection connection, Object object) {
                    Gdx.app.postRunnable(() -> {
                        if (object instanceof KryoMessage) {
                            statusLabel.setText("Received: " + ((KryoMessage) object).getObj());
                        }
                    });
                }
            });
            client.start();
            connectToServer();
        }
    }

    private void sendLogin(String email, String password) {
        HashMap<String, String> keys = new HashMap<>();
        keys.put("email", email);
        keys.put("password", password);
        AuthRequest request = new AuthRequest(AuthModeType.LOGIN, keys);
        client.sendTCP(new KryoMessage(KryoMessageType.AUTH, null, request));
        statusLabel.setText("Login request sent");
    }

    private void sendRegister(String email, String password, String password2, String username) {
        HashMap<String, String> keys = new HashMap<>();
        keys.put("email", email);
        keys.put("password", password);
        keys.put("password2", password2);
        keys.put("username", username);
        AuthRequest request = new AuthRequest(AuthModeType.REGISTER, keys);
        client.sendTCP(new KryoMessage(KryoMessageType.AUTH, null, request));
        statusLabel.setText("Register request sent");
    }

    @Override
    public void render() {
        Gdx.gl.glClearColor(0.1f, 0.1f, 0.15f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        stage.act(Gdx.graphics.getDeltaTime());
        stage.draw();
    }

    @Override
    public void dispose() {
        stage.dispose();
        skin.dispose();
        if (client != null) client.stop();
    }
}
