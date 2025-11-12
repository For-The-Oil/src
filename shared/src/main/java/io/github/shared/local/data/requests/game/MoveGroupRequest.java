package io.github.shared.local.data.requests.game;

import com.badlogic.gdx.math.Vector2;

import java.util.ArrayList;

import io.github.shared.local.data.EnumsTypes.RequestType;
import io.github.shared.local.data.requests.Request;

public class MoveGroupRequest extends Request {
    private ArrayList<Integer> group;
    private float posX;
    private float posY;

    public MoveGroupRequest(){}
    public MoveGroupRequest(RequestType request) {
        super(request);
    }

}
