package io.github.android.utils;

import android.util.Log;

import java.util.HashMap;

import io.github.android.manager.ClientManager;
import io.github.android.manager.SessionManager;
import io.github.shared.local.data.EnumsTypes.KryoMessageType;
import io.github.shared.local.data.EnumsTypes.SyncType;
import io.github.shared.local.data.network.KryoMessage;
import io.github.shared.local.data.requests.SynchronizeRequest;

public  final class NetworkUtils {

    public static void askForFullGameSync() {
        Log.d("For The Oil", "Asking for full game sync");
        SynchronizeRequest request = new SynchronizeRequest(SyncType.FULL_RESYNC, new HashMap<>());
        KryoMessage kryo = new KryoMessage(KryoMessageType.SYNC, SessionManager.getInstance().getToken(), request);
        ClientManager.getInstance().getKryoManager().send(kryo);
    }


}
