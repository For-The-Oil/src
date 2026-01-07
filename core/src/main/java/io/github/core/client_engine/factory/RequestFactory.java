package io.github.core.client_engine.factory;

import java.util.ArrayList;
import java.util.HashMap;

import io.github.shared.data.enums_types.AuthModeType;
import io.github.shared.data.enums_types.Direction;
import io.github.shared.data.enums_types.EntityType;
import io.github.shared.data.enums_types.GameModeType;
import io.github.shared.data.enums_types.MatchModeType;
import io.github.shared.data.requests.AuthRequest;
import io.github.shared.data.requests.DeckRequest;
import io.github.shared.data.requests.MatchMakingRequest;
import io.github.shared.data.enums_types.DeckRequestType;
import io.github.shared.data.requests.game.AttackGroupRequest;
import io.github.shared.data.requests.game.BuildRequest;
import io.github.shared.data.requests.game.DestroyRequest;
import io.github.shared.data.requests.game.MoveGroupRequest;
import io.github.shared.data.requests.game.SummonRequest;


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

    public static BuildRequest createBuildRequest(EntityType type, int netFrom, float posX, float posY, Direction direction){
        return new BuildRequest(type, netFrom, posX, posY, direction);
    }

    public static DestroyRequest createdDestroyRequest(ArrayList<Integer> toKill){
        DestroyRequest request = new DestroyRequest();
        request.setToKill(toKill);
        return request;
    }

    /**
     * Creates a SummonRequest to produce units from a building.
     *
     * @param type       The EntityType of the unit to produce
     * @param netFrom    The network ID of the building producing the unit
     * @param quantities Number of units to produce
     * @return SummonRequest ready to be packed and sent
     */
    public static SummonRequest createSummonRequest(EntityType type, int netFrom, int quantities) {
        return new SummonRequest(type, netFrom, quantities);
    }

    public static MoveGroupRequest createMoveGroupRequest(ArrayList<Integer> group, float x, float y, boolean force) {
        return new MoveGroupRequest(group, x, y, force);
    }

    public static AttackGroupRequest createAttackGroupRequest(ArrayList<Integer> group, int targetNetId) {
        AttackGroupRequest request = new AttackGroupRequest();
        request.setGroup(group);
        request.setTargetNetId(targetNetId);
        return request;
    }



    // Plus tard tu pourras ajouter d'autres méthodes pour GameRequest, DeckRequest, etc.
}
