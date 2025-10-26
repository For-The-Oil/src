package io.github.shared.local.data.requests;

import java.util.ArrayList;

import io.github.shared.local.data.EnumsTypes.RequestType;

public class DestroyRequest extends Request{

    ArrayList<Integer> entities;

    public DestroyRequest(){}
    public DestroyRequest(RequestType request) {
        super(request);
    }


}
