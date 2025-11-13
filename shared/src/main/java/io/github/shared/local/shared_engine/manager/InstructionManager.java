package io.github.shared.local.shared_engine.manager;

import com.artemis.ComponentMapper;
import com.artemis.Entity;

import io.github.shared.local.data.EnumsTypes.EntityType;
import io.github.shared.local.data.IGame;
import io.github.shared.local.data.component.NetComponent;
import io.github.shared.local.data.component.OnCreationComponent;
import io.github.shared.local.data.component.ProprietyComponent;
import io.github.shared.local.data.gameobject.Shape;
import io.github.shared.local.data.instructions.CreateInstruction;
import io.github.shared.local.data.instructions.DestroyInstruction;
import io.github.shared.local.data.instructions.EventsInstruction;
import io.github.shared.local.data.instructions.Instruction;
import io.github.shared.local.data.instructions.ResourcesInstruction;
import io.github.shared.local.data.instructions.UpdateEntityInstruction;
import io.github.shared.local.data.network.Player;
import io.github.shared.local.data.snapshot.EntitySnapshot;
import io.github.shared.local.shared_engine.Utility;
import io.github.shared.local.shared_engine.factory.EntityFactory;

public class InstructionManager {
    public static void executeInstruction(Instruction instruction, IGame game){
        String type = instruction.getClass().getSimpleName();
        try {
            switch (type) {
                case "CreateInstruction":
                    CreateInstruction ci = (CreateInstruction) instruction;

                    ComponentMapper<NetComponent> netMapper = game.getWorld().getMapper(NetComponent.class);
                    ComponentMapper<OnCreationComponent> onCreateMapper = game.getWorld().getMapper(OnCreationComponent.class);
                    ComponentMapper<ProprietyComponent> proprietyMapper = game.getWorld().getMapper(ProprietyComponent.class);
                    for (int i = 0; i < ci.getToSpawn().size(); i++) {
                        int x = ci.getPosX().get(i);
                        int y = ci.getPosY().get(i);
                        int netId = ci.getNetId().get(i);
                        int from = ci.getFrom().get(i);
                        EntityType entityType = ci.getToSpawn().get(i);
                        Entity entity = game.getWorld().createEntity();

                        Player player = Utility.findPlayerByUuid(game.getPlayersList(), ci.getPlayer().get(i));
                        if (player != null) {
                            Utility.subtractResourcesInPlace(player.getResources(), entityType.getCost());
                            ProprietyComponent prc = proprietyMapper.create(entity);
                            prc.set(player.getUuid(), Utility.findTeamByPlayer(player, game.getPlayerTeam()));
                        }

                        if (entityType.getType().equals(EntityType.Type.Building)) {
                            ShapeManager.overlayShape(game.getMap(), new Shape(entityType.getShapeType().getShape(), netId), x, y);
                        }


                        NetComponent nc = netMapper.create(entity);
                        nc.set(netId, entityType);

                        OnCreationComponent occ = onCreateMapper.create(entity);
                        occ.set(x, y, from, entityType.getCreate_time());
                    }
                    break;

                case "UpdateEntityInstruction":
                    for (EntitySnapshot snapshot : ((UpdateEntityInstruction) (instruction)).getToUpdate()) {
                        EntityFactory.applySnapshotToEntity(game.getWorld(), snapshot);
                    }
                    break;
                case "DestroyInstruction":
                    for (int netId : ((DestroyInstruction) (instruction)).getToKill()) {
                        EntityFactory.destroyEntityByNetId(game.getWorld(), netId);
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
        }
        catch (Exception e){
            System.err.print("executeInstruction err "+e);
        }
    }
}



