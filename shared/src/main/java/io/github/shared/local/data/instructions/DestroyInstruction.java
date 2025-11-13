package io.github.shared.local.data.instructions;

import java.util.ArrayList;

public class DestroyInstruction extends Instruction{

    //NetID indiquant quels entités à détruire
    private ArrayList<Integer> toKill;
    public DestroyInstruction(){}
    public DestroyInstruction(long timestamp){
        super(timestamp);
        this.toKill = new ArrayList<>();
    }

    public ArrayList<Integer> getToKill() {
        return toKill;
    }

    public void add(int netId){
        this.toKill.add(netId);
    }

    public void setToKill(ArrayList<Integer> toKill) {
        this.toKill = toKill;
    }
}
