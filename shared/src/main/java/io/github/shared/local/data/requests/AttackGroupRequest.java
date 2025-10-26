package io.github.shared.local.data.requests;

import java.util.ArrayList;

import io.github.shared.local.data.EnumsTypes.RequestType;

public class AttackGroupRequest extends Request {

    private ArrayList<Integer> group;
    private int target;

    public AttackGroupRequest(){}
    public AttackGroupRequest(RequestType request) {
        super(request);
    }


}
