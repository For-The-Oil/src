package io.github.server.server_engine.manager;

import com.esotericsoftware.kryonet.Connection;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import io.github.server.data.network.ServerNetwork;
import io.github.shared.local.data.EnumsTypes.DeckRequestType;
import io.github.shared.local.data.network.ClientNetwork;
import io.github.shared.local.data.requests.DeckRequest;

public final class DeckManager {

    private static final DeckManager INSTANCE = new DeckManager();

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    public static DeckManager getInstance() {
        return INSTANCE;
    }

    public void handleDeckRequest(Connection connection, DeckRequest request){

        ClientNetwork client = ServerNetwork.getInstance().getClientByConnection(connection);
        if(client==null) return;

        DeckRequestType type = request.getMode();

        switch (type) {
            case DELETE_DECK:
                break;

            case CHANGE_CURRENT_DECK:
                break;

            case CREATE_DECK:
                break;

            case MODIFY_DECK:
                break;

            default:
                break;
        }

    }








}
