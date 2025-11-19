package io.github.shared.data.requests.game;

import io.github.shared.data.EnumsTypes.Direction;
import io.github.shared.data.EnumsTypes.EntityType;
import io.github.shared.data.requests.Request;

public class BuildRequest extends Request {
    private EntityType type;
    private int from;
    private int posX;
    private int posY;
    private Direction direction;

    public BuildRequest(){
        super();
    }
    public BuildRequest(EntityType type, int from, int posX, int posY, Direction direction){
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

    public int getPosX() {
        return posX;
    }

    public void setPosX(int posX) {
        this.posX = posX;
    }

    public int getPosY() {
        return posY;
    }

    public void setPosY(int posY) {
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
