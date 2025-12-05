package io.github.server.game_engine.manager;

import com.artemis.Entity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import io.github.shared.data.IGame;
import io.github.shared.data.component.LifeComponent;
import io.github.shared.data.component.NetComponent;
import io.github.shared.data.component.PositionComponent;
import io.github.shared.data.enums_types.Direction;
import io.github.shared.data.enums_types.EntityType;
import io.github.shared.data.enums_types.ResourcesType;
import io.github.shared.data.gameobject.Shape;
import io.github.shared.data.instructions.CreateInstruction;
import io.github.shared.data.instructions.DestroyInstruction;
import io.github.shared.data.instructions.Instruction;
import io.github.shared.data.instructions.UpdateEntityInstruction;
import io.github.shared.data.requests.Request;
import io.github.shared.data.requests.game.AttackGroupRequest;
import io.github.shared.data.requests.game.BuildRequest;
import io.github.shared.data.requests.game.CastRequest;
import io.github.shared.data.requests.game.DestroyRequest;
import io.github.shared.data.requests.game.MoveGroupRequest;
import io.github.shared.data.requests.game.SummonRequest;
import io.github.shared.data.snapshot.ComponentSnapshot;
import io.github.shared.data.snapshot.EntitySnapshot;
import io.github.shared.shared_engine.Utility;
import io.github.shared.shared_engine.manager.EcsManager;
import io.github.shared.shared_engine.manager.ShapeManager;

public class RequestGameManager {
    public static Instruction executeGameRequest(Request request, IGame game) {
        try {
            String type = request.getClass().getSimpleName();
            switch (type) {
                case "AttackGroupRequest":
                    return handleGameAttackGroupRequest((AttackGroupRequest) request,game);
                case "MoveGroupRequest":
                    return handleGameMoveGroupRequest((MoveGroupRequest) request,game);
                case "BuildRequest":
                    return handleGameBuildRequest((BuildRequest) request,game);
                case "CastRequest":
                    return handleGameCastRequest((CastRequest) request,game);
                case "SummonRequest":
                    return handleGameSummonRequest((SummonRequest) request,game);
                case "DestroyRequest":
                    return handleGameDestroyRequest((DestroyRequest) request,game);
                default:
                    throw new IllegalArgumentException("Request non pris en charge : " + type);
            }
        } catch (Exception e) {
            System.err.print("executeRequest err " + e);
        }
        return null;
    }

    private static Instruction handleGameAttackGroupRequest(AttackGroupRequest attackGroupRequest ,IGame game){
        UpdateEntityInstruction updateEntityAttack = new UpdateEntityInstruction(System.currentTimeMillis());

        Entity entityTarget = EcsManager.findEntityByNetId(game.getWorld(),attackGroupRequest.getTargetNetId());
        if (entityTarget == null)return null;
        PositionComponent posTarget = game.getWorld().getMapper(PositionComponent.class).get(entityTarget);
        LifeComponent lifeTarget = game.getWorld().getMapper(LifeComponent.class).get(entityTarget);
        if (posTarget == null || lifeTarget == null) return null;

        for (int netId : attackGroupRequest.getGroup()){
            Entity entityAttack = EcsManager.findEntityByNetIdAndPlayer(game.getWorld(), netId, attackGroupRequest.getPlayer());
            if (entityAttack == null) continue;
            NetComponent netAttack = game.getWorld().getMapper(NetComponent.class).get(entityAttack);

            HashMap<String,Object> fields = new HashMap<>();
            fields.put("targetId",netId);
            fields.put("nextTargetId",-1);
            fields.put("force",true);
            updateEntityAttack.getToUpdate().add(new EntitySnapshot(netId,netAttack.entityType, new ArrayList<>(Collections.singleton(new ComponentSnapshot("TargetComponent", fields)))));
        }

        if(!updateEntityAttack.getToUpdate().isEmpty()){
            return updateEntityAttack;
        }
        return null;
    }

    private static Instruction handleGameMoveGroupRequest(MoveGroupRequest moveGroupRequest ,IGame game){
        UpdateEntityInstruction updateEntityMove = new UpdateEntityInstruction(System.currentTimeMillis());

        if(moveGroupRequest.getPosX()<0||moveGroupRequest.getPosY()<0)return null;

        for (int netId : moveGroupRequest.getGroup()){
            Entity entityMove = EcsManager.findEntityByNetIdPlayerAndType(game.getWorld(), netId, moveGroupRequest.getPlayer(), EntityType.Type.Unit);
            if (entityMove == null) continue;
            NetComponent netMove = game.getWorld().getMapper(NetComponent.class).get(entityMove);

            HashMap<String,Object> fields = new HashMap<>();
            fields.put("targetRelated",moveGroupRequest.isTargetRelated());
            fields.put("destinationX",moveGroupRequest.getPosX());
            fields.put("destinationY",moveGroupRequest.getPosY());
            fields.put("nextX1",-1);fields.put("nextY1",-1);fields.put("nextX2",-1);fields.put("nextY2",-1);
            fields.put("force",true);

            updateEntityMove.getToUpdate().add(new EntitySnapshot(netId, netMove.entityType, new ArrayList<>(Collections.singleton(new ComponentSnapshot("MoveComponent", fields)))));
        }

        if(!updateEntityMove.getToUpdate().isEmpty()) {
            return updateEntityMove;
        }
        return null;
    }

    private static Instruction handleGameBuildRequest(BuildRequest buildRequest ,IGame game){
        CreateInstruction buildInstruction = new CreateInstruction(System.currentTimeMillis());

        EntityType entityTypeBuild = buildRequest.getType();
        Shape shape = entityTypeBuild.getShapeType().getShape();
        Direction direction = buildRequest.getDirection();

        if(!ShapeManager.canOverlayShape(game.getMap(), ShapeManager.rotateShape(shape, direction), buildRequest.getPosX(), buildRequest.getPosY(), 0, 0, shape.getWidth(), shape.getHeight(),entityTypeBuild.getShapeType().getCanBePlacedOn())
            || !Utility.canSubtractResources(entityTypeBuild.getCost(), entityTypeBuild.getCost())
            || (entityTypeBuild.getFrom()!=null
            && EcsManager.findEntityByNetIdPlayerAndEntityType(game.getWorld(), buildRequest.getFrom(), buildRequest.getPlayer(), entityTypeBuild.getFrom()) == null)) return null;

        buildInstruction.add(entityTypeBuild,direction, Utility.getNetId(), buildRequest.getFrom(), buildRequest.getPosX(), buildRequest.getPosY(), buildRequest.getPlayer());

        if (!buildInstruction.getToSpawn().isEmpty()) {
            return buildInstruction;
        }
        return null;
    }

    private static Instruction handleGameCastRequest(CastRequest castRequest ,IGame game){
        CreateInstruction castInstruction = new CreateInstruction(System.currentTimeMillis());

        EntityType entityTypeCast = castRequest.getType();

        if(!Utility.canSubtractResources(entityTypeCast.getCost(), entityTypeCast.getCost())) return null;
        Entity entityCastFrom = EcsManager.findEntityByNetIdPlayerAndEntityType(game.getWorld(), castRequest.getFrom(), castRequest.getPlayer(), entityTypeCast.getFrom());
        if (entityCastFrom == null
            || game.getWorld().getMapper(PositionComponent.class).get(entityCastFrom) == null) return null;

        castInstruction.add(entityTypeCast,null, Utility.getNetId(), castRequest.getFrom(), castRequest.getTargetX(), castRequest.getTargetY(), castRequest.getPlayer());

        if (!castInstruction.getToSpawn().isEmpty()) {
            return castInstruction;
        }
        return null;
    }

    private static Instruction handleGameSummonRequest(SummonRequest summonRequest ,IGame game){
        CreateInstruction createInstructionSummon = new CreateInstruction(System.currentTimeMillis());

        HashMap<ResourcesType, Integer> toSubtractSummon = new HashMap<>();
        EntityType entityTypeSummon = summonRequest.getType();

        for(int i = summonRequest.getQuantities() ; i>0 ; i--) {
            Utility.addResourcesInPlace(toSubtractSummon, entityTypeSummon.getCost());

            if(!Utility.canSubtractResources(entityTypeSummon.getCost(), toSubtractSummon))continue;
            Entity entitySummonFrom = EcsManager.findEntityByNetIdPlayerAndEntityType(game.getWorld(), summonRequest.getFrom(), summonRequest.getPlayer(), entityTypeSummon.getFrom());
            if (entitySummonFrom == null) continue;
            PositionComponent posSummon = game.getWorld().getMapper(PositionComponent.class).get(entitySummonFrom);
            if (posSummon == null) return null;

            createInstructionSummon.add(entityTypeSummon,null, Utility.getNetId(), summonRequest.getFrom(), posSummon.x, posSummon.y, summonRequest.getPlayer());
        }

        if (!createInstructionSummon.getToSpawn().isEmpty()){
            return createInstructionSummon;
        }
        return null;
    }

    private static Instruction handleGameDestroyRequest(DestroyRequest destroyRequest ,IGame game){
        DestroyInstruction destroyInstruction = new DestroyInstruction(System.currentTimeMillis());

        for (int netId : destroyRequest.getToKill()) {
            if (null == EcsManager.findEntityByNetIdAndPlayer(game.getWorld(), netId, destroyRequest.getPlayer()))continue;
            destroyInstruction.add(netId);
        }

        if (!destroyInstruction.getToKill().isEmpty()){
            return destroyInstruction;
        }
        return null;
    }

}
