package io.github.core.client_engine.factory;

import io.github.shared.data.enums_types.KryoMessageType;
import io.github.shared.data.network.KryoMessage;
import io.github.shared.data.requests.AuthRequest;
import io.github.shared.data.requests.DeckRequest;
import io.github.shared.data.requests.MatchMakingRequest;
import io.github.shared.data.requests.game.BuildRequest;
import io.github.shared.data.requests.game.DestroyRequest;
import io.github.shared.data.requests.game.SummonRequest;

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

    public static KryoMessage packBuildRequest(BuildRequest request, String token){
        if (request == null) return null;
        return new KryoMessage(KryoMessageType.GAME_REQUEST, token, request);
    }

    public static KryoMessage packDestroyRequest(DestroyRequest request, String token){
        if(request ==null) return null;
        return new KryoMessage(KryoMessageType.GAME_REQUEST, token, request);
    }

    /**
     * Wraps a SummonRequest into a KryoMessage for unit production.
     *
     * @param request the SummonRequest to wrap
     * @param token   the user session token
     * @return KryoMessage ready to be sent
     */
    public static KryoMessage packSummonRequest(SummonRequest request, String token) {
        if (request == null) return null;
        return new KryoMessage(KryoMessageType.GAME_REQUEST, token, request);
    }



    // TODO : Add even more way of packaging the Requests

}
