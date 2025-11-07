package io.github.shared.local.data.network;

import com.esotericsoftware.kryo.Kryo;

import java.util.HashMap;

import io.github.shared.local.data.EnumsTypes.AuthModeType;
import io.github.shared.local.data.EnumsTypes.GameModeType;
import io.github.shared.local.data.EnumsTypes.KryoMessageType;
import io.github.shared.local.data.EnumsTypes.MatchModeType;
import io.github.shared.local.data.EnumsTypes.RequestType;
import io.github.shared.local.data.requests.AuthRequest;
import io.github.shared.local.data.requests.MatchMakingRequest;

public final class KryoRegistry {
    public static void registerAll(Kryo kryo) {
        kryo.register(String.class);
        kryo.register(HashMap.class);
        kryo.register(KryoMessage.class);
        kryo.register(AuthRequest.class);
        kryo.register(AuthModeType.class);
        kryo.register(RequestType.class);
        kryo.register(KryoMessageType.class);
        kryo.register(MatchMakingRequest.class);
        kryo.register(MatchModeType.class);
        kryo.register(GameModeType.class);
    }
}
