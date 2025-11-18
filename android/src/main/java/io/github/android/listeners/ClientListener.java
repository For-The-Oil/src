package io.github.android.listeners;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;

import io.github.android.activity.BaseActivity;
import io.github.android.activity.LoginActivity;
import io.github.android.manager.ClientManager;
import io.github.android.manager.SessionManager;
import io.github.android.utils.RedirectUtils;
import io.github.shared.local.data.network.KryoMessage;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

/**
 * ClientListener with flexible functional callbacks support,
 * including UI thread flag per callback.
 */
public class ClientListener extends Listener {

    private static ClientListener INSTANCE;

    private BaseActivity currentActivity;
    private final Map<Class<?>, CallbackWrapper> messageHandlers = new HashMap<>();
    private final ExecutorService executor = Executors.newFixedThreadPool(4);

    public static ClientListener getInstance() {
        if (INSTANCE == null) INSTANCE = new ClientListener();
        return INSTANCE;
    }

    private ClientListener() {}


    private static class CallbackWrapper {
        final Consumer<Object> handler;
        final boolean onUIThread;

        CallbackWrapper(Consumer<Object> handler, boolean onUIThread) {
            this.handler = handler;
            this.onUIThread = onUIThread;
        }
    }

    /**
     * Callback appelé lorsque le client établit une connexion avec le serveur.
     *
     * @param connection la connexion nouvellement établie
     */
    @Override
    public void connected(Connection connection) {
        Log.d("For the Oil", "Connected to server!");
    }

    /**
     * Callback appelé lorsque la connexion est fermée (par le client ou le serveur).
     *
     * @param connection la connexion qui vient d'être fermée
     */
    @Override
    public void disconnected(Connection connection) {
        Log.d("For the Oil", "Disconnected from server.");
        SessionManager.getInstance().clearSession();
        ClientManager.getInstance().getKryoManager().closeClient();
        RedirectUtils.simpleRedirectAndClearStack(this.currentActivity, LoginActivity.class, "login_error", "Connection lost...");
    }


    public void setCurrentActivity(BaseActivity activity) {
        this.currentActivity = activity;
    }

    /**
     * Register a callback for a specific message type.
     *
     * <p>This allows you to handle messages of type {@code messageClass} when received
     * from the network. The callback can be executed either on the UI thread or on a
     * background thread depending on the {@code onUIThread} flag.</p>
     *
     * <p><strong>Example usage:</strong></p>
     * <pre>{@code
     * clientListener.onMessage(MyPayload.class, payload -> {
     *     // This code runs whenever a MyPayload is received
     *     Log.d("ClientListener", "Received value: " + payload.getValue());
     * }, true); // true = execute on UI thread
     * }</pre>
     *
     * @param messageClass class of the payload
     * @param handler callback to execute
     * @param onUIThread if true, callback is posted to main thread
     */
    public <T> void onMessage(Class<T> messageClass, Consumer<T> handler, boolean onUIThread) {
        messageHandlers.put(messageClass, new CallbackWrapper(obj -> handler.accept(messageClass.cast(obj)), onUIThread));
    }


    /**
     * Callback invoked when a network message is received from the KryoNet connection.
     *
     * <p>This method handles incoming messages wrapped in a {@link KryoMessage}. It extracts the
     * payload from the message and dispatches it to the appropriate registered handler based on
     * the payload's class.</p>
     *
     * <p>Execution logic:</p>
     * <ul>
     *   <li>If a {@link CallbackWrapper} is registered for the payload's class:
     *     <ul>
     *       <li>If {@code onUIThread} is {@code true}, the handler is posted to the Android
     *           main thread using {@link android.os.Handler}.</li>
     *       <li>If {@code onUIThread} is {@code false}, the handler is executed asynchronously
     *           using the internal {@link ExecutorService} to avoid blocking the KryoNet
     *           network thread.</li>
     *     </ul>
     *   </li>
     *   <li>If no handler is registered for the payload's class, the message is ignored.</li>
     * </ul>
     *
     * <p>Notes:</p>
     * <ul>
     *   <li>Always checks for null messages or payloads to avoid {@link NullPointerException}.</li>
     *   <li>This design allows handlers to choose whether to execute on the UI thread or a
     *       background thread, providing flexibility for CPU-intensive processing without
     *       blocking the network listener.</li>
     * </ul>
     *
     * @param connection the KryoNet connection from which the message was received
     * @param object the received object, expected to be a {@link KryoMessage}
     */
    @Override
    public void received(Connection connection, Object object) {
        if (!(object instanceof KryoMessage)) return;
        KryoMessage msg = (KryoMessage) object;
        Object payload = msg.getObj();
        if (payload == null) return;

        CallbackWrapper wrapper = messageHandlers.get(payload.getClass());
        if (wrapper != null) {
            if (wrapper.onUIThread) {
                postToMain(() -> wrapper.handler.accept(payload)); //Execution on the UI thread
            } else {
                executor.submit(() -> wrapper.handler.accept(payload)); //Execution on the Executor threads
            }
        }
    }


    /**
     * Removes the registered callback for a specific message type.
     *
     * @param messageClass the class of the payload whose callback should be removed
     */
    public void removeCallback(Class<?> messageClass) {
        messageHandlers.remove(messageClass);
    }

    /**
     * Clears all registered message callbacks.
     */
    public void clearCallbacks() {
        messageHandlers.clear();
    }

    private void postToMain(Runnable r) {
        new Handler(Looper.getMainLooper()).post(() -> {
            if (!currentActivity.isFinishing() && !currentActivity.isDestroyed()) {
                r.run();
            }
        });
    }


}
