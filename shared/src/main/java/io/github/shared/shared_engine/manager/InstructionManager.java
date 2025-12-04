package io.github.shared.shared_engine.manager;

import com.artemis.ComponentMapper;
import com.artemis.Entity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import io.github.shared.data.component.RessourceComponent;
import io.github.shared.data.enums_types.Direction;
import io.github.shared.data.enums_types.EntityType;
import io.github.shared.data.enums_types.ResourcesType;
import io.github.shared.data.IGame;
import io.github.shared.data.component.BuildingMapPositionComponent;
import io.github.shared.data.component.FreezeComponent;
import io.github.shared.data.component.LifeComponent;
import io.github.shared.data.component.MoveComponent;
import io.github.shared.data.component.NetComponent;
import io.github.shared.data.component.OnCreationComponent;
import io.github.shared.data.component.PositionComponent;
import io.github.shared.data.component.ProjectileComponent;
import io.github.shared.data.component.ProprietyComponent;
import io.github.shared.data.gameobject.Shape;
import io.github.shared.data.instructions.CreateInstruction;
import io.github.shared.data.instructions.DestroyInstruction;
import io.github.shared.data.instructions.EventsInstruction;
import io.github.shared.data.instructions.Instruction;
import io.github.shared.data.instructions.ResourcesInstruction;
import io.github.shared.data.instructions.UpdateEntityInstruction;
import io.github.shared.data.network.Player;
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
import io.github.shared.shared_engine.factory.EntityFactory;

public class InstructionManager {
    public static void executeInstruction(Instruction instruction, IGame game) {
        String type = instruction.getClass().getSimpleName();
        try {
            switch (type) {
                case "CreateInstruction":
                    CreateInstruction ci = (CreateInstruction) instruction;

                    ComponentMapper<NetComponent> netMapper = game.getWorld().getMapper(NetComponent.class);
                    ComponentMapper<ProprietyComponent> proprietyMapper = game.getWorld().getMapper(ProprietyComponent.class);
                    ComponentMapper<OnCreationComponent> onCreateMapper = game.getWorld().getMapper(OnCreationComponent.class);
                    ComponentMapper<PositionComponent> positionMapper = game.getWorld().getMapper(PositionComponent.class);
                    ComponentMapper<BuildingMapPositionComponent> buildingMapPositionMapper = game.getWorld().getMapper(BuildingMapPositionComponent.class);
                    ComponentMapper<LifeComponent> lifeMapper = game.getWorld().getMapper(LifeComponent.class);
                    ComponentMapper<FreezeComponent> freezeMapper = game.getWorld().getMapper(FreezeComponent.class);
                    ComponentMapper<ProjectileComponent> projectileMapper = game.getWorld().getMapper(ProjectileComponent.class);
                    ComponentMapper<MoveComponent> moveMapper = game.getWorld().getMapper(MoveComponent.class);
                    ComponentMapper<RessourceComponent> resMapper = game.getWorld().getMapper(RessourceComponent.class);

                    for (int i = 0; i < ci.getToSpawn().size(); i++) {
                        float x = ci.getPosX().get(i);
                        float y = ci.getPosY().get(i);
                        int netId = ci.getNetId().get(i);
                        int from = ci.getFrom().get(i);
                        Direction direction = ci.getDirections().get(i);
                        EntityType entityType = ci.getToSpawn().get(i);
                        Entity entity = game.getWorld().createEntity();

                        Player player = Utility.findPlayerByUuid(game.getPlayersList(), ci.getPlayer().get(i));
                        if (player != null) {
                            Utility.subtractResourcesInPlace(player.getResources(), entityType.getCost());
                            ProprietyComponent prc = proprietyMapper.create(entity);
                            prc.set(player.getUuid(), Utility.findTeamByPlayer(player, game.getPlayerTeam()));
                        }

                        if (entityType.getType().equals(EntityType.Type.Building)) {
                            Shape overlay = new Shape(entityType.getShapeType().getShape(), netId);
                            ShapeManager.overlayShape(game.getMap(), ShapeManager.rotateShape(overlay, direction), (int) x, (int) y, 0, 0, overlay.getWidth(), overlay.getHeight());
                            game.setMapDirty(true);

                            BuildingMapPositionComponent bpc = buildingMapPositionMapper.create(entity);
                            bpc.set(Utility.worldToCell(x),Utility.worldToCell(y),direction);

                            PositionComponent posC = positionMapper.create(entity);
                            posC.set(x,y,0);

                            if(entityType.getProduction()!= null){
                                RessourceComponent res = resMapper.create(entity);
                                res.set(new HashMap<>(entityType.getProduction()));
                            }
                        }

                        if(entityType.getType().equals(EntityType.Type.Building)||entityType.getType().equals(EntityType.Type.Unit)){

                            FreezeComponent fc= freezeMapper.create(entity);
                            fc.freeze_time = entityType.getFreeze_time();

                            LifeComponent lc = lifeMapper.create(entity);
                            lc.set(entityType.getMaxHealth(),entityType.getMaxHealth(),entityType.getArmor(),entityType.getPassiveHeal());

                            OnCreationComponent occ = onCreateMapper.create(entity);
                            occ.set(from, entityType.getCreate_time());

                        }

                        if(entityType.getType().equals(EntityType.Type.Projectile)){
                            PositionComponent posFromC = Utility.getPositionByNetId(game.getWorld(), from,netMapper,positionMapper);

                            PositionComponent posC = positionMapper.create(entity);
                            posC.set(posFromC.x, posFromC.y,0);

                            ProjectileComponent pc = projectileMapper.create(entity);
                            pc.set(entityType,entityType.getDamage(),entityType.getAoe(),entityType.getMaxHeight(),posFromC.x,posFromC.y);

                            MoveComponent mc = moveMapper.create(entity);
                            mc.set(false,x,y,true);
                        }

                        NetComponent nc = netMapper.create(entity);
                        nc.set(netId, entityType);
                    }
                    break;

                case "UpdateEntityInstruction":
                    for (EntitySnapshot snapshot : ((UpdateEntityInstruction) (instruction)).getToUpdate()) {
                        EntityFactory.applySnapshotToEntity(game.getWorld(), snapshot);
                    }
                    break;
                case "DestroyInstruction":
                    for (int netId : ((DestroyInstruction) (instruction)).getToKill()) {
                        EntityFactory.destroyEntityByNetId(game.getWorld(), netId, game);
                    }
                    break;
                case "ResourcesInstruction":
                    ResourcesInstruction resourcesInstruction = (ResourcesInstruction) instruction;
                    for (Player player : game.getPlayersList()) {
                        if (player.getUuid().equals(resourcesInstruction.getPlayer())) {
                            player.setResources(resourcesInstruction.getRessources());
                        }
                    }
                    break;
                case "EventsInstruction":
                    game.setCurrentEvent(((EventsInstruction) (instruction)).getEventType());
                    break;
                case "FinalInstruction":
                    break;
                case "SpecialRequestsInstruction":
                    break;
                default:
                    throw new IllegalArgumentException("Instruction non pris en charge : " + type);
            }
        } catch (Exception e) {
            System.err.print("executeInstruction err " + e);
        }
    }

    public static Instruction executeGameRequest(Request request, IGame game) {
        Instruction instruction = null;
        String type = request.getClass().getSimpleName();
        try {
            switch (type) {
                case "AttackGroupRequest":
                    AttackGroupRequest attackGroupRequest = (AttackGroupRequest) request;
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

                    if(!updateEntityAttack.getToUpdate().isEmpty()) instruction = updateEntityAttack;
                    break;
                case "MoveGroupRequest":
                    MoveGroupRequest moveGroupRequest = (MoveGroupRequest) request;
                    UpdateEntityInstruction updateEntityMove = new UpdateEntityInstruction(System.currentTimeMillis());

                    if(moveGroupRequest.getPosX()<0||moveGroupRequest.getPosY()<0)return null;

                    for (int netId : moveGroupRequest.getGroup()){
                        Entity entityMove = EcsManager.findEntityByNetIdPlayerAndType(game.getWorld(), netId, moveGroupRequest.getPlayer(),EntityType.Type.Unit);
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

                    if(!updateEntityMove.getToUpdate().isEmpty()) instruction = updateEntityMove;
                    break;
                case "BuildRequest":
                    BuildRequest buildRequest = (BuildRequest) request;
                    CreateInstruction buildInstruction = new CreateInstruction(System.currentTimeMillis());

                    EntityType entityTypeBuild = buildRequest.getType();
                    Shape shape = entityTypeBuild.getShapeType().getShape();
                    Direction direction = buildRequest.getDirection();

                    if(!ShapeManager.canOverlayShape(game.getMap(), ShapeManager.rotateShape(shape, direction), buildRequest.getPosX(), buildRequest.getPosY(), 0, 0, shape.getWidth(), shape.getHeight(),entityTypeBuild.getShapeType().getCanBePlacedOn())
                        || !Utility.canSubtractResources(entityTypeBuild.getCost(), entityTypeBuild.getCost())
                        || (entityTypeBuild.getFrom()!=null
                        && EcsManager.findEntityByNetIdPlayerAndEntityType(game.getWorld(), buildRequest.getFrom(), buildRequest.getPlayer(), entityTypeBuild.getFrom()) == null)) return null;

                    buildInstruction.add(entityTypeBuild,direction, Utility.getNetId(), buildRequest.getFrom(), buildRequest.getPosX(), buildRequest.getPosY(), buildRequest.getPlayer());

                    if (!buildInstruction.getToSpawn().isEmpty()) instruction = buildInstruction;
                    break;
                case "CastRequest":
                    CastRequest castRequest = (CastRequest) request;
                    CreateInstruction castInstruction = new CreateInstruction(System.currentTimeMillis());

                    EntityType entityTypeCast = castRequest.getType();

                    if(!Utility.canSubtractResources(entityTypeCast.getCost(), entityTypeCast.getCost())) return null;
                    Entity entityCastFrom = EcsManager.findEntityByNetIdPlayerAndEntityType(game.getWorld(), castRequest.getFrom(), castRequest.getPlayer(), entityTypeCast.getFrom());
                    if (entityCastFrom == null
                        || game.getWorld().getMapper(PositionComponent.class).get(entityCastFrom) == null) return null;

                    castInstruction.add(entityTypeCast,null, Utility.getNetId(), castRequest.getFrom(), castRequest.getTargetX(), castRequest.getTargetY(), castRequest.getPlayer());

                    if (!castInstruction.getToSpawn().isEmpty()) instruction = castInstruction;
                    break;
                case "SummonRequest":
                    SummonRequest summonRequest = (SummonRequest) request;
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

                    if (!createInstructionSummon.getToSpawn().isEmpty()) instruction = createInstructionSummon;
                    break;
                case "DestroyRequest":
                    DestroyRequest destroyRequest = (DestroyRequest) request;
                    DestroyInstruction destroyInstruction = new DestroyInstruction(System.currentTimeMillis());

                    for (int netId : destroyRequest.getToKill()) {
                        if (null == EcsManager.findEntityByNetIdAndPlayer(game.getWorld(), netId, destroyRequest.getPlayer()))continue;
                        destroyInstruction.add(netId);
                    }

                    if (!destroyInstruction.getToKill().isEmpty()) instruction = destroyInstruction;
                    break;
                default:
                    throw new IllegalArgumentException("Request non pris en charge : " + type);
            }
        } catch (Exception e) {
            System.err.print("executeRequest err " + e);
        }
        return instruction;
    }

}



