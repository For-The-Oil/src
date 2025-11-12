package io.github.shared.local.data.instructions;

import java.util.ArrayList;

import io.github.shared.local.data.snapshot.EntitySnapshot;

public class UpdateEntityInstruction extends Instruction{
    //Composants Sérializer associé à une NetID
    private ArrayList<EntitySnapshot> toUpdate;

    public UpdateEntityInstruction(){}
    public UpdateEntityInstruction(long timestamp) {
        super(timestamp);
    }

    public ArrayList<EntitySnapshot> getToUpdate() {
        return toUpdate;
    }

    public void setToUpdate(ArrayList<EntitySnapshot> toUpdate) {
        this.toUpdate = toUpdate;
    }
}
