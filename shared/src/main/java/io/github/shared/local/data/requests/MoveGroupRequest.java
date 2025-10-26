package io.github.shared.local.data.requests;

import com.badlogic.gdx.math.Vector2;

import java.util.ArrayList;

import io.github.shared.local.data.EnumsTypes.RequestType;

public class MoveGroupRequest extends Request{

    private ArrayList<Integer> group;
    private Vector2 pos;

    public MoveGroupRequest(){}
    public MoveGroupRequest(RequestType request) {
        super(request);
    }

}
