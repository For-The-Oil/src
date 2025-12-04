package io.github.server.server_engine.kryolistener;

import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;

import io.github.server.server_engine.manager.MatchmakingManager;
import io.github.shared.data.enums_types.GameModeType;
import io.github.shared.data.enums_types.MatchModeType;
import io.github.shared.data.network.KryoMessage;
import io.github.shared.data.requests.MatchMakingRequest;

public class MatchMakingListener extends Listener {

    @Override
    public void received(Connection connection, Object object) {
        if (object instanceof KryoMessage) {
            KryoMessage msg = (KryoMessage) object;

            if (msg.getObj() instanceof MatchMakingRequest) {

                MatchMakingRequest request = (MatchMakingRequest) msg.getObj();
                handleMatchMaking(connection, request, msg.getToken());
            }
        }
    }
    private void handleMatchMaking(Connection connection, MatchMakingRequest req, String token) {

//        if(PlayerChecker.isValidToken(connection, token)==null){
//            connection.close();
//            return;
//        }


        GameModeType mode = req.getGameMode();
        MatchModeType matchType = req.getCommand();

        System.out.println(" " + connection.getRemoteAddressTCP().toString() + " "+ req.getCommand() + " "+ req.getGameMode());

        //TODO : We must check if the mode given by the player is correct

        switch (matchType) {
            case ASK:
                // Ajouter le joueur à la file d’attente pour le gameMode
                addToQueue(connection, mode);
                break;
            case CANCEL:
                // Retirer le joueur de la file d’attente
                removeFromQueue(connection, mode);
                break;
            default:
                // Fallback
        }
    }

    public void addToQueue(Connection connection, GameModeType mode) {
        MatchmakingManager.getInstance().addToQueue(connection, mode);
    }

    private void removeFromQueue(Connection connection, GameModeType mode) {
        MatchmakingManager.getInstance().removeFromQueue(connection, mode);
    }



}
