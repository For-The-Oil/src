package io.github.core.client_engine.manager;

import static io.github.core.config.ServerDefaultConfig.CONNECT_TIMEOUT;

import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Listener;

import java.io.IOException;

import io.github.core.config.ClientDefaultConfig;
import io.github.shared.local.data.network.KryoMessage;
import io.github.shared.local.data.network.KryoRegistry;
import jdk.jpackage.internal.Log;

/**
 * <h1>Class KryoClientManager</h1>
 *
 * <h2>Purpose</h2>
 * <p>Small layer that help manage the connection between a client and the server.</p>
 *
 * <h2>Example of use</h2>
 * <p>In this example we will add two listeners and connect ourself to the localhost at the port 54555.
 * Also, we will give to our connection the CallBack MyConnectedCallback class.</p>
 * <pre>
 * {@code
 * KryoClientManager kryoClient = new KryoClientManager();
 *
 * kryoClient.addListener(new MyFirstListener());
 * kryoClient.addListener(new MyAwesomeListener());
 *
 * try {
 *     kryoClient.start();
 * }
 * catch (IllegalStateException e){ ... }
 *
 * manager.connect("127.0.0.1", 54555, new MyConnectedCallback());
 *
 *
 * manager.send( new KryoMessage( ... ) );
 *
 * ...
 *
 * manager.disconnect();
 *
 * }
 * </pre>
 *
 *
 * @see com.esotericsoftware.kryonet.Client
 * @see KryoRegistry
 *
 */
public class KryoClientManager {

    /** The KryoNet client instance responsible for network communication. */
    private final Client client;

    /** Indicates whether the client is currently connected to the server. */
    private boolean connected = false;

    /**
     * <h1>Constructor of the class</h1>
     *
     * <p>Init a Kryo Client and register into Kryo all the Serialized objects.</p>
     */
    public KryoClientManager() {
        client = new Client();
        KryoRegistry.registerAll(client.getKryo());
    }

    /**
     * <h1>Start function</h1>
     *
     * <p>Start the client if not already connected.</p>
     */
    public void start() throws IllegalStateException {
        if (client.isConnected()) {
            throw new IllegalStateException("Client is already started or connected.");
        }
        client.start();
    }


    /**
     * <h1>With Listener</h1>
     *
     * <p>Add a listener and return self.</p>
     * <p>Allows code such as :</p>
     * <pre>
     *     {@code
     *     myManager.withListener(new MyListener).start();
     *     }
     * </pre>
     *
     *
     * @param listener the listener that will be added
     * @throws IllegalArgumentException
     */
    public KryoClientManager withListener(Listener listener) throws IllegalArgumentException{
        if (listener == null) {
            throw new IllegalArgumentException("Listener cannot be null");
        }
        client.addListener(listener);
        return this;
    }

    /**
     * <h1>Add Listener</h1>
     *
     * <p>Same as the {@link #withListener(Listener)} but withtout the return self.</p>
     *
     * @param listener the listener that will be added
     * @throws IllegalArgumentException
     */
    public void addListener(Listener listener) throws IllegalArgumentException{
        if (listener == null) {
            throw new IllegalArgumentException("Listener cannot be null");
        }
        client.addListener(listener);
    }

    /**
     * <h1>Remove Listener</h1>
     *
     * <p>Removes a previously added listener from the Kryo client.</p>
     *
     * @param listener the listener to remove
     * @throws IllegalArgumentException if listener is null
     */
    public void removeListener(Listener listener) {
        if (listener == null) throw new IllegalArgumentException("Listener cannot be null");
        client.removeListener(listener);
    }

    /**
     * <h1>Disconnect</h1>
     * <p>Close the current connection and all of the sub process.</p>
     */
    public void stop() {
        try {
            if (client != null && connected) {
                client.stop();
                connected = false;
                System.out.println("Client disconnected successfully.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * <h1>Connect</h1>
     *
     * <p>Try to open a new connection with the server.</p>
     *
     * @param host the IP
     * @param port the Port
     */
    public void connect(String host, int port, Runnable onSuccess, Runnable onFailure) {
        new Thread(() -> {
            try {
                client.connect(CONNECT_TIMEOUT, host, port); // 5000 ms timeout
                connected = true;
                if (onSuccess != null) {
                    onSuccess.run();
                }
            } catch (IOException e) {
                connected = false;
                e.printStackTrace();
                if (onFailure != null) {
                    onFailure.run();
                }
            }
        }).start();
    }




    /**
     * <h1>Send</h1>
     *
     * <p>Function that allows the sending of a {@link KryoMessage}.</p>
     *
     * @param message the {@link KryoMessage} object
     */
    public void send(KryoMessage message) {
        if (connected) client.sendTCP(message);
    }

    /**
     * <h3>Checks whether the client is connected.</h3>
     *
     * @return true if the client is connected, false otherwise
     */
    public boolean isConnected() { return connected; }

    /**
     * <h3>Returns the internal KryoNet {@link Client} instance.</h3>
     *
     * @return the active Kryo client
     */
    public Client getClient() { return client; }

    /**
     * <h1>Close the Client</h1>
     */
    public void closeClient() {
        client.close();
    }
}
