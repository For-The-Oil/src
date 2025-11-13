package io.github.shared.local.data.network;

import com.esotericsoftware.kryo.Kryo;

import java.util.HashMap;

import io.github.shared.local.data.EnumsTypes.AuthModeType;
import io.github.shared.local.data.EnumsTypes.GameModeType;
import io.github.shared.local.data.EnumsTypes.InstructionType;
import io.github.shared.local.data.EnumsTypes.KryoMessageType;
import io.github.shared.local.data.EnumsTypes.MatchModeType;
import io.github.shared.local.data.EnumsTypes.RequestType;
import io.github.shared.local.data.IGame;
import io.github.shared.local.data.NetGame;
import io.github.shared.local.data.instructions.CreateInstruction;
import io.github.shared.local.data.instructions.DestroyInstruction;
import io.github.shared.local.data.instructions.EventsInstruction;
import io.github.shared.local.data.instructions.FinalInstruction;
import io.github.shared.local.data.instructions.Instruction;
import io.github.shared.local.data.instructions.ResourcesInstruction;
import io.github.shared.local.data.instructions.SpecialRequestsInstruction;
import io.github.shared.local.data.instructions.UpdateEntityInstruction;
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
        kryo.register(InstructionType.class);
        kryo.register(Instruction.class);
        kryo.register(CreateInstruction.class);
        kryo.register(DestroyInstruction.class);
        kryo.register(EventsInstruction.class);
        kryo.register(FinalInstruction.class);
        kryo.register(ResourcesInstruction.class);
        kryo.register(SpecialRequestsInstruction.class);
        kryo.register(UpdateEntityInstruction.class);
        kryo.register(IGame.class);
        kryo.register(NetGame.class);
        kryo.register(NetGame.class);
    }
}
