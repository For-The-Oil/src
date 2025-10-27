package io.github.shared.local.data.instructions;

import java.util.ArrayList;

import io.github.shared.local.data.snapshot.EntitySnapshot;

public class CreateInstruction extends Instruction{
    //Composants Sérializer associé à une NetID
    private ArrayList<EntitySnapshot> toSpawn;
    public CreateInstruction(){}
    public CreateInstruction(long timestamp) {
        super(timestamp);
        this.toSpawn = new ArrayList<EntitySnapshot>();
    }
}
