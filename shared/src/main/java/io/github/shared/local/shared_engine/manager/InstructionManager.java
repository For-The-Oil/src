package io.github.shared.local.shared_engine.manager;

import io.github.shared.local.data.IGame;
import io.github.shared.local.data.instructions.DestroyInstruction;
import io.github.shared.local.data.instructions.EventsInstruction;
import io.github.shared.local.data.instructions.Instruction;
import io.github.shared.local.data.instructions.ResourcesInstruction;
import io.github.shared.local.data.instructions.UpdateEntityInstruction;
import io.github.shared.local.data.network.Player;
import io.github.shared.local.data.snapshot.EntitySnapshot;
import io.github.shared.local.shared_engine.factory.EntityFactory;

public class InstructionManager {
    public static void executeInstruction(Instruction instruction, IGame game){
        String type = instruction.getClass().getSimpleName();
        switch (type) {
            case "UpdateEntityInstruction":
                for (EntitySnapshot snapshot : ((UpdateEntityInstruction)(instruction)).getToUpdate()){
                    EntityFactory.applySnapshotToEntity(game.getWorld(),snapshot);
                }
                break;
            case "DestroyInstruction":
                for (int netId : ((DestroyInstruction)(instruction)).getToKill()){
                    EntityFactory.destroyEntityByNetId(game.getWorld(),netId);
                }
                break;
            case "ResourcesInstruction":
                ResourcesInstruction resourcesInstruction = (ResourcesInstruction) instruction;
                for (Player player : game.getPlayersList()){
                    if(player.getUuid().equals(resourcesInstruction.getPlayer())){
                        player.setRessources(resourcesInstruction.getRessources());
                    }
                }
                break;
            case "EventsInstruction":
                game.setCurrentEvent(((EventsInstruction)(instruction)).getEventType());
                break;
            case "FinalInstruction":
                break;
            case "SpecialRequestsInstruction":
                break;
            default:
                throw new IllegalArgumentException("Instruction non pris en charge : " + type);
        }
    }
}
