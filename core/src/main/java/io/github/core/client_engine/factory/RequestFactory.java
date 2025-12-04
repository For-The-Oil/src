package io.github.core.client_engine.factory;

import java.util.HashMap;

import io.github.shared.data.enums_types.AuthModeType;
import io.github.shared.data.enums_types.GameModeType;
import io.github.shared.data.enums_types.MatchModeType;
import io.github.shared.data.requests.AuthRequest;
import io.github.shared.data.requests.DeckRequest;
import io.github.shared.data.requests.MatchMakingRequest;
import io.github.shared.data.enums_types.DeckRequestType;


/**
 * <h1>RequestFactory</h1>
 * <p>
 * Factory class to centralize the creation of network requests (AuthRequest, GameRequest, etc.).
 * This helps keep the code clean, maintainable, and reduces duplication.
 * </p>
 */
public class RequestFactory {

    /**
     * Creates an AuthRequest for login.
     *
     * @param email    user's email
     * @param password user's password
     * @return AuthRequest ready to be sent via KryoClientManager
     */
    public static AuthRequest createLoginRequest(String email, String password) {
        HashMap<String, String> keys = new HashMap<>();
        keys.put("email", email);
        keys.put("password", password);
        return new AuthRequest(AuthModeType.LOGIN, keys);
    }

    /**
     * Creates an AuthRequest for registration.
     *
     * @param email      user's email
     * @param username   chosen username
     * @param password   password
     * @param password2  password confirmation
     * @return AuthRequest ready to be sent via KryoClientManager
     */
    public static AuthRequest createRegisterRequest(String email, String username, String password, String password2) {
        HashMap<String, String> keys = new HashMap<>();
        keys.put("email", email);
        keys.put("username", username);
        keys.put("password", password);
        keys.put("password2", password2);
        return new AuthRequest(AuthModeType.REGISTER, keys);
    }


    public static MatchMakingRequest createAskMatchmakingRequest(GameModeType mode, HashMap<String, String> key){
        return createMatchmakingRequest(mode, MatchModeType.ASK, key);
    }

    public static MatchMakingRequest createCancelMatchmakingRequest(GameModeType mode, HashMap<String, String> key){
        return createMatchmakingRequest(mode, MatchModeType.CANCEL, key);
    }

    public static MatchMakingRequest createMatchmakingRequest(GameModeType mode, MatchModeType type, HashMap<String, String> key){
        return new MatchMakingRequest(type,mode, key);
    }

    public static DeckRequest createDeckRequest(DeckRequestType mode, HashMap<String, String> keys) {
        DeckRequest request = new DeckRequest();
        request.setMode(mode);
        request.setKeys(keys);
        return request;
    }





    // Plus tard tu pourras ajouter d'autres méthodes pour GameRequest, DeckRequest, etc.
}
