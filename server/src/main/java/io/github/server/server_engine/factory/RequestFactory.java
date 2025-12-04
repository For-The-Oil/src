package io.github.server.server_engine.factory;

import java.util.HashMap;

import io.github.shared.data.enums_types.GameModeType;
import io.github.shared.data.enums_types.MatchModeType;
import io.github.shared.data.enums_types.SyncType;
import io.github.shared.data.NetGame;
import io.github.shared.data.network.ClientNetwork;
import io.github.shared.data.requests.DeckRequest;
import io.github.shared.data.requests.MatchMakingRequest;
import io.github.shared.data.requests.SynchronizeRequest;
import io.github.shared.data.enums_types.DeckRequestType;


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



    public static SynchronizeRequest createSynchronizeRequest(ClientNetwork client, NetGame game){
        HashMap<String, Object> map = new HashMap<>();
        map.put("game",game);
        return new SynchronizeRequest(SyncType.FULL_RESYNC, map);
    }


    public static DeckRequest createDeckRequest(DeckRequestType mode, HashMap<String, String> map){
        return new DeckRequest(mode, map);
    }







}
