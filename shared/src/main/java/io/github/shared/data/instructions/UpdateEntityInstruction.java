package io.github.shared.data.instructions;

import java.util.ArrayList;

import io.github.shared.data.snapshot.EntitySnapshot;

public class UpdateEntityInstruction extends Instruction{
    //Composants Sérializer associé à une NetID
    private ArrayList<EntitySnapshot> toUpdate;

    public UpdateEntityInstruction(){}
    public UpdateEntityInstruction(long timestamp) {
        super(timestamp);
        this.toUpdate = new ArrayList<>();
    }

    public ArrayList<EntitySnapshot> getToUpdate() {
        return toUpdate;
    }

    public void setToUpdate(ArrayList<EntitySnapshot> toUpdate) {
        this.toUpdate = toUpdate;
    }
}
