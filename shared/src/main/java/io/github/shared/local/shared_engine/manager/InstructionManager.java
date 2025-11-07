package io.github.shared.local.shared_engine.manager;

import io.github.shared.local.data.instructions.Instruction;

public class InstructionManager {
    public static void executeInstruction(Instruction instruction){
        String type = instruction.getClass().getSimpleName();
        switch (type) {
            case "CreateInstruction":
                break;
            case "DestroyInstruction":
                break;
            case "EventsInstruction":
                break;
            case "FinalInstruction":
                break;
            case "ResourcesInstruction":
                break;
            case "UpdateEntityInstruction":
                break;
            case "SpecialRequestsInstruction":
                break;
            default:
                throw new IllegalArgumentException("Instruction non pris en charge : " + type);
        }
    }
}
