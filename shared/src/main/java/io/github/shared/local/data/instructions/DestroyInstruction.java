package io.github.shared.local.data.instructions;

import java.util.ArrayList;

public class DestroyInstruction extends Instruction{

    //NetID indiquant quels entités à détruire
    private ArrayList<Integer> toKill;
    public DestroyInstruction(){}
    public DestroyInstruction(long timestamp,ArrayList<Integer> toKill){
        super(timestamp);
        this.toKill = toKill;
    }

    public ArrayList<Integer> getToKill() {
        return toKill;
    }
}
