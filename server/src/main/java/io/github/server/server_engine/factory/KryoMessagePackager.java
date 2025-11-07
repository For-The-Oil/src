package io.github.server.server_engine.factory;

import io.github.shared.local.data.EnumsTypes.KryoMessageType;
import io.github.shared.local.data.network.KryoMessage;
import io.github.shared.local.data.requests.AuthRequest;
import io.github.shared.local.data.requests.MatchMakingRequest;

/**
 * Packager pour créer des KryoMessage côté serveur.
 * Centralise la conversion des requêtes serveur en KryoMessage envoyables.
 */
public class KryoMessagePackager {

    /**
     * Pack une AuthRequest dans un KryoMessage.
     *
     * @param request l'AuthRequest à envoyer
     * @return KryoMessage prêt à être envoyé via KryoNet
     */
    public static KryoMessage packAuthRequest(AuthRequest request) {
        return new KryoMessage(KryoMessageType.AUTH, null, request);
    }

    /**
     * Pack une MatchMakingRequest dans un KryoMessage.
     *
     * @param request la MatchMakingRequest à envoyer
     * @return KryoMessage prêt à être envoyé via KryoNet
     */
    public static KryoMessage packMatchMakingRequest(MatchMakingRequest request) {
        return new KryoMessage(KryoMessageType.MATCHMAKING, null, request);
    }

    // Plus tard, tu pourras ajouter d'autres méthodes pour GameRequest, DeckRequest, etc.
}
