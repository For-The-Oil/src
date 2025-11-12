package io.github.shared.local.data.requests.game;

import java.util.ArrayList;

import io.github.shared.local.data.EnumsTypes.RequestType;
import io.github.shared.local.data.requests.Request;

public class AttackGroupRequest extends Request {
    private ArrayList<Integer> group;
    private int targetNetId;

    public AttackGroupRequest(){}
    public AttackGroupRequest(RequestType request) {
        super(request);
    }


}
