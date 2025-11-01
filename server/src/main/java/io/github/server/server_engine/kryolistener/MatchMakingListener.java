package io.github.server.server_engine.kryolistener;

import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;

import io.github.server.server_engine.utils.PlayerChecker;
import io.github.shared.local.data.EnumsTypes.GameModeType;
import io.github.shared.local.data.EnumsTypes.MatchModeType;
import io.github.shared.local.data.network.KryoMessage;
import io.github.shared.local.data.requests.MatchMakingRequest;

import java.util.HashMap;

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

//        if(!PlayerChecker.isValidToken(connection, token)){
//            connection.close();
//            return;
//        }


        GameModeType mode = req.getGameMode();
        MatchModeType matchType = req.getCommand();

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

    private void addToQueue(Connection connection, GameModeType mode) {
        // TODO : logique serveur pour stocker le joueur dans une file selon le mode
        System.out.println("Ajout à la queue : " + connection.getID() + " Mode: " + mode);
    }

    private void removeFromQueue(Connection connection, GameModeType mode) {
        // TODO : logique serveur pour retirer le joueur de la file
        System.out.println("Retiré de la queue : " + connection.getID() + " Mode: " + mode);
    }



}
