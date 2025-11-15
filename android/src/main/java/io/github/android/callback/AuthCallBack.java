package io.github.android.callback;

import io.github.shared.data.requests.AuthRequest;

public interface AuthCallBack {
    default void onRegisterSuccess(AuthRequest req) {}
    default void onRegisterFailure(AuthRequest req) {}

    default void onLoginSuccess(AuthRequest req) {}
    default void onLoginFailure(AuthRequest req) {}

    default void onTokenSuccess(AuthRequest req) {}
    default void onTokenFailure(AuthRequest req) {}
}
