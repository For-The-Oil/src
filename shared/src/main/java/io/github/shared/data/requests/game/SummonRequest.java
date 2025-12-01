package io.github.shared.data.requests.game;

import io.github.shared.data.enumsTypes.EntityType;
import io.github.shared.data.requests.Request;

public class SummonRequest extends Request {
    private EntityType type;
    private int from;
    private int quantities;

    public SummonRequest(){
        super();
    }
    public SummonRequest(EntityType type, int from, int quantities){
        super();
        this.type = type;
        this.from = from;
        this.quantities = quantities;
    }

    public EntityType getType() {
        return type;
    }

    public void setType(EntityType type) {
        this.type = type;
    }

    public int getFrom() {
        return from;
    }

    public void setFrom(int from) {
        this.from = from;
    }

    public int getQuantities() {
        return quantities;
    }

    public void setQuantities(int quantities) {
        this.quantities = quantities;
    }
}
