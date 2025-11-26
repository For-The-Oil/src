package io.github.core.client_engine.factory;

import io.github.shared.data.EnumsTypes.KryoMessageType;
import io.github.shared.data.network.KryoMessage;
import io.github.shared.data.requests.AuthRequest;
import io.github.shared.data.requests.DeckRequest;
import io.github.shared.data.requests.MatchMakingRequest;

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

    public static KryoMessage packAuthRequest(MatchMakingRequest matchRequest, String token) {
        if (matchRequest == null) {
            throw new IllegalArgumentException("MatchMakingRequest cannot be null");
        }
        return new KryoMessage(KryoMessageType.MATCHMAKING, null, matchRequest);
    }


    public static KryoMessage packDeckRequest(DeckRequest request, String token) {
        if (request == null) return null;
        return new KryoMessage(KryoMessageType.DECK, token, request);
    }



    // TODO : Add even more way of packaging the Requests

}
