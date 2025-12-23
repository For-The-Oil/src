package io.github.shared.data.requests.game;

import io.github.shared.data.enums_types.Direction;
import io.github.shared.data.enums_types.EntityType;
import io.github.shared.data.requests.Request;

public class BuildRequest extends Request {
    private EntityType type;
    private int from;
    private float posX;
    private float posY;
    private Direction direction;

    public BuildRequest(){
        super();
    }
    public BuildRequest(EntityType type, int from, float posX, float posY, Direction direction){
        super();
        this.type = type;
        this.from = from;
        this.posX = posX;
        this.posY = posY;
        this.direction = direction;
    }

    public EntityType getType() {
        return type;
    }

    public void setType(EntityType type) {
        this.type = type;
    }

    public float getPosX() {
        return posX;
    }

    public void setPosX(float posX) {
        this.posX = posX;
    }

    public float getPosY() {
        return posY;
    }

    public void setPosY(float posY) {
        this.posY = posY;
    }

    public int getFrom() {
        return from;
    }

    public void setFrom(int from) {
        this.from = from;
    }

    public Direction getDirection() {
        return direction;
    }

    public void setDirection(Direction direction) {
        this.direction = direction;
    }
}
