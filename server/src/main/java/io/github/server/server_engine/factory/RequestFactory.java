package io.github.server.server_engine.factory;

import java.util.HashMap;

import io.github.shared.local.data.EnumsTypes.GameModeType;
import io.github.shared.local.data.EnumsTypes.MatchModeType;
import io.github.shared.local.data.requests.MatchMakingRequest;

/**
 * Factory côté serveur pour créer des MatchMakingRequest.
 */
public class RequestFactory {

    /**
     * Crée une MatchMakingRequest pour confirmer la connection à une file de matchmaking.
     *
     * @param mode le GameModeType choisi
     * @param message message optionnel à envoyer au client
     * @return MatchMakingRequest prête à être packagée dans un KryoMessage
     */
    public static MatchMakingRequest createMatchMakingRequest(GameModeType mode, String message, MatchModeType type) {
        HashMap<String, String> keys = new HashMap<>();
        if (message != null) keys.put("message", message);
        return new MatchMakingRequest(type, mode, keys);
    }

    public static MatchMakingRequest createMatchMakingRequest(GameModeType mode, String message, MatchModeType type, HashMap<String, String> keys) {
        if (message != null) keys.put("message", message);
        return new MatchMakingRequest(type, mode, keys);
    }

    /**
     * Crée une MatchMakingRequest pour quitter une file de matchmaking.
     *
     * @param mode le GameModeType choisi
     * @param message message optionnel à envoyer au client
     * @return MatchMakingRequest prête à être packagée dans un KryoMessage
     */
    public static MatchMakingRequest createLeaveQueueRequest(GameModeType mode, String message) {
        HashMap<String, String> keys = new HashMap<>();
        if (message != null) keys.put("message", message);
        return new MatchMakingRequest(MatchModeType.LEAVE, mode, keys);
    }

    /**
     * Crée une MatchMakingRequest générique avec des informations personnalisées.
     *
     * @param command le type de commande MatchModeType
     * @param mode le GameModeType
     * @param keys données supplémentaires à envoyer
     * @return MatchMakingRequest prête à être packagée
     */
    public static MatchMakingRequest createCustomRequest(MatchModeType command, GameModeType mode, HashMap<String, String> keys) {
        return new MatchMakingRequest(command, mode, keys);
    }
}
