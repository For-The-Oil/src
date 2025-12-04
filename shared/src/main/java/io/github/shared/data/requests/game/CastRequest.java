package io.github.shared.data.requests.game;

import io.github.shared.data.enums_types.EntityType;
import io.github.shared.data.requests.Request;

public class CastRequest extends Request {
    private int from;
    private float targetX;
    private float targetY;
    private EntityType type;

    public CastRequest(){
        super();
    }
    public CastRequest(int from, float targetX, float targetY, EntityType type){super();
        this.from = from;
        this.targetX = targetX;
        this.targetY = targetY;
        this.type = type;
    }

    public int getFrom() {
        return from;
    }

    public void setFrom(int from) {
        this.from = from;
    }

    public float getTargetX() {
        return targetX;
    }

    public void setTargetX(float targetX) {
        this.targetX = targetX;
    }

    public float getTargetY() {
        return targetY;
    }

    public void setTargetY(float targetY) {
        this.targetY = targetY;
    }

    public EntityType getType() {
        return type;
    }

    public void setType(EntityType type) {
        this.type = type;
    }
}
