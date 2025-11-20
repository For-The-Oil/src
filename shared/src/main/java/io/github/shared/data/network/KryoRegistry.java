package io.github.shared.data.network;

import com.artemis.World;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.serializers.DefaultSerializers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

import io.github.shared.data.EnumsTypes.AuthModeType;
import io.github.shared.data.EnumsTypes.CellEffectType;
import io.github.shared.data.EnumsTypes.CellType;
import io.github.shared.data.EnumsTypes.DeckCardCategory;
import io.github.shared.data.EnumsTypes.Direction;
import io.github.shared.data.EnumsTypes.EntityType;
import io.github.shared.data.EnumsTypes.EventType;
import io.github.shared.data.EnumsTypes.GameModeType;
import io.github.shared.data.EnumsTypes.KryoMessageType;
import io.github.shared.data.EnumsTypes.MapName;
import io.github.shared.data.EnumsTypes.MatchModeType;
import io.github.shared.data.EnumsTypes.RessourcesType;
import io.github.shared.data.EnumsTypes.ShapeType;
import io.github.shared.data.EnumsTypes.SyncType;
import io.github.shared.data.EnumsTypes.WeaponType;
import io.github.shared.data.IGame;
import io.github.shared.data.NetGame;
import io.github.shared.data.gameobject.Cell;
import io.github.shared.data.gameobject.DamageEntry;
import io.github.shared.data.gameobject.Deck;
import io.github.shared.data.gameobject.Shape;
import io.github.shared.data.instructions.CreateInstruction;
import io.github.shared.data.instructions.DestroyInstruction;
import io.github.shared.data.instructions.EventsInstruction;
import io.github.shared.data.instructions.FinalInstruction;
import io.github.shared.data.instructions.Instruction;
import io.github.shared.data.instructions.ResourcesInstruction;
import io.github.shared.data.instructions.SpecialRequestsInstruction;
import io.github.shared.data.instructions.UpdateEntityInstruction;
import io.github.shared.data.requests.AuthRequest;
import io.github.shared.data.requests.DeckRequest;
import io.github.shared.data.requests.MatchMakingRequest;
import io.github.shared.data.requests.SynchronizeRequest;
import io.github.shared.data.snapshot.ComponentSnapshot;
import io.github.shared.data.snapshot.EntitySnapshot;
import io.github.shared.local.data.EnumsTypes.DeckRequestType;
import io.github.shared.local.data.EnumsTypes.RequestType;

public final class KryoRegistry {
    public static void registerAll(Kryo kryo) {


        //basics
        kryo.register(String.class);
        kryo.register(HashMap.class);
        kryo.register(ArrayList.class);
        //kryo.register(Queue.class);
        kryo.register(UUID.class, new DefaultSerializers.UUIDSerializer());


        //requests
        kryo.register(KryoMessage.class);
        kryo.register(AuthRequest.class);
        kryo.register(MatchMakingRequest.class);
        kryo.register(SynchronizeRequest.class);
        kryo.register(DeckRequest.class);


        //instructions
        kryo.register(Instruction.class);
        kryo.register(CreateInstruction.class);
        kryo.register(DestroyInstruction.class);
        kryo.register(EventsInstruction.class);
        kryo.register(FinalInstruction.class);
        kryo.register(ResourcesInstruction.class);
        kryo.register(SpecialRequestsInstruction.class);
        kryo.register(UpdateEntityInstruction.class);



        //types
        kryo.register(AuthModeType.class);
        kryo.register(CellEffectType.class);
        kryo.register(CellType.class);
        kryo.register(DeckCardCategory.class);
        kryo.register(Direction.class);
        kryo.register(EntityType.class);
        kryo.register(EventType.class);
        kryo.register(GameModeType.class);
        //kryo.register(InstructionType.class);
        kryo.register(KryoMessageType.class);
        kryo.register(MapName.class);
        kryo.register(MatchModeType.class);
        //kryo.register(ProjectileType.class);
        //kryo.register(RequestType.class);
        kryo.register(RessourcesType.class);
        kryo.register(ShapeType.class);
        kryo.register(SyncType.class);
        kryo.register(WeaponType.class);
        kryo.register(DeckRequestType.class);


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
