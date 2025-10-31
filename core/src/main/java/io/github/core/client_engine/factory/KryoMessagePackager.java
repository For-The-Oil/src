package io.github.core.client_engine.factory;

import io.github.shared.local.data.EnumsTypes.KryoMessageType;
import io.github.shared.local.data.network.KryoMessage;
import io.github.shared.local.data.requests.AuthRequest;

/**
 * <h1>KryoMessagePackager</h1>
 * <p>
 * Utility class to wrap different request objects into KryoMessages
 * before sending them through KryoClientManager.
 * </p>
 */
public class KryoMessagePackager {

    /**
     * Wraps an AuthRequest into a KryoMessage.
     *
     * @param authRequest the AuthRequest to wrap
     * @return KryoMessage ready to be sent
     */
    public static KryoMessage packAuthRequest(AuthRequest authRequest) {
        if (authRequest == null) {
            throw new IllegalArgumentException("AuthRequest cannot be null");
        }
        return new KryoMessage(KryoMessageType.AUTH, null, authRequest);
    }

    // TODO : Add even more way of packaging the Requests

}
