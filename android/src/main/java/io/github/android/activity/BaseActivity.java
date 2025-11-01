package io.github.android.activity;

import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

/**
 * {@code BaseActivity}
 *
 * <p>
 * A minimal base class extending {@link androidx.appcompat.app.AppCompatActivity}
 * that provides a safe way to execute cleanup logic before finishing an activity.
 * Subclasses may override {@link #onBeforeKill()} for resource release or state saving,
 * and call {@link #safeKill()} to ensure proper cleanup before {@link #finish()}.
 * </p>
 *
 * <p><strong>Usage example:</strong></p>
 * <pre>{@code
 * public class LoginActivity extends BaseActivity {
 *     @Override
 *     protected void onBeforeKill() {
 *         ClientManager.getInstance().disconnect();
 *     }
 * }
 *
 * // Safely finish:
 * loginActivity.safeKill();
 * }</pre>
 *
 * <p>
 * {@link #safeKill()} wraps {@link #onBeforeKill()} in a try/catch to prevent crashes
 * and guarantees that {@link #finish()} is always executed.
 * </p>
 */
public abstract class BaseActivity extends AppCompatActivity {

    /**
     * Hook called immediately before the Activity is finished when {@link #safeKill()} is used.
     *
     * <p>
     * Subclasses should override this method to perform cleanup work such as:
     * <ul>
     *   <li>unregistering broadcast receivers</li>
     *   <li>stopping/interrupting background threads</li>
     *   <li>closing network connections or sockets</li>
     *   <li>persisting transient UI/state to storage</li>
     * </ul>
     * </p>
     *
     * <p><strong>Important:</strong> Keep this method short and avoid long-blocking operations on the
     * main thread. If you must perform lengthy cleanup, start an asynchronous task and ensure it completes
     * before calling {@link #safeKill()}.</p>
     */
    protected void onBeforeKill() {
        // Optional — subclasses can override if needed.
    }


    /**
     * Safely finishes the Activity after executing {@link #onBeforeKill()}.
     *
     * <p>
     * This method guarantees that {@link #onBeforeKill()} is invoked and protects the call with a
     * {@code try/catch} so that any exception thrown by cleanup code does not prevent the Activity
     * from being finished. Use this method instead of calling {@link #finish()} directly when you need
     * to ensure cleanup runs first.
     * </p>
     *
     * <p><strong>Example:</strong>
     * <pre>{@code
     * // perform cleanup defined in onBeforeKill(), then finish the activity
     * safeKill();
     * }</pre>
     * </p>
     */
    public void safeKill() {
        try {
            onBeforeKill();
        } catch (Exception e) {
            Log.e("ForTheOil", "Error during onBeforeKill(): ", e);
        } finally {
            finish();
        }
    }
}
