package io.github.shared.data.network;

import com.artemis.World;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.serializers.DefaultSerializers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

import io.github.shared.data.EnumsTypes.*;
import io.github.shared.data.IGame;
import io.github.shared.data.NetGame;
import io.github.shared.data.gameobject.*;
import io.github.shared.data.instructions.*;
import io.github.shared.data.requests.*;
import io.github.shared.data.requests.game.AttackGroupRequest;
import io.github.shared.data.requests.game.BuildRequest;
import io.github.shared.data.requests.game.CastRequest;
import io.github.shared.data.requests.game.DestroyRequest;
import io.github.shared.data.requests.game.MoveGroupRequest;
import io.github.shared.data.requests.game.SummonRequest;
import io.github.shared.data.snapshot.*;

public final class KryoRegistry {
    public static void registerAll(Kryo kryo) {


        //basics
        kryo.register(String.class);
        kryo.register(HashMap.class);
        kryo.register(ArrayList.class);
        //kryo.register(Queue.class);
        kryo.register(UUID.class, new DefaultSerializers.UUIDSerializer());


        //Network
        kryo.register(KryoMessage.class);


        //requests (Network)
        kryo.register(AuthRequest.class);
        kryo.register(DeckRequest.class);
        kryo.register(MatchMakingRequest.class);
        kryo.register(Request.class);
        kryo.register(SpecialRequest.class);
        kryo.register(SynchronizeRequest.class);


        //requests (Game)
        kryo.register(AttackGroupRequest.class);
        kryo.register(BuildRequest.class);
        kryo.register(CastRequest.class);
        kryo.register(DestroyRequest.class);
        kryo.register(MoveGroupRequest.class);
        kryo.register(SummonRequest.class);


        //instructions
        kryo.register(Instruction.class);
        kryo.register(CreateInstruction.class);
        kryo.register(DestroyInstruction.class);
        kryo.register(EventsInstruction.class);
        kryo.register(FinalInstruction.class);
        kryo.register(ResourcesInstruction.class);
        kryo.register(SpecialRequestsInstruction.class);
        kryo.register(UpdateEntityInstruction.class);
        kryo.register(DeckRequestType.class);



        //types
        kryo.register(AuthModeType.class);
        kryo.register(CellEffectType.class);
        kryo.register(CellType.class);
        kryo.register(DeckCardCategory.class);
        kryo.register(Direction.class);
        kryo.register(EntityType.class);
        kryo.register(EventType.class);
        kryo.register(GameModeType.class);
        kryo.register(KryoMessageType.class);
        kryo.register(MapName.class);
        kryo.register(MatchModeType.class);
        kryo.register(ResourcesType.class);
        kryo.register(ShapeType.class);
        kryo.register(SyncType.class);
        kryo.register(WeaponType.class);


        //Objects
        kryo.register(IGame.class);
        kryo.register(NetGame.class);
        kryo.register(Cell.class);
        kryo.register(Cell[].class);
        kryo.register(Cell[][].class);
        kryo.register(DamageEntry.class);
        kryo.register(Deck.class);
        kryo.register(Shape.class);
        kryo.register(Player.class);
        kryo.register(EntitySnapshot.class);
        kryo.register(ComponentSnapshot.class);
        kryo.register(World.class);

    }
}
