package io.github.android.callback;

import android.widget.TextView;

import io.github.android.utils.UiUtils;

public class FailConnectionCallback implements Runnable {

    private final TextView textView;
    private final String message;

    public FailConnectionCallback(TextView textView, String message) {
        this.textView = textView;
        this.message = message;
    }

    @Override
    public void run() {
        textView.post(() -> UiUtils.showMessage(textView, message));
    }
}
