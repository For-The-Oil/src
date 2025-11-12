package io.github.shared.local.data.requests.game;

import java.util.ArrayList;

import io.github.shared.local.data.EnumsTypes.RequestType;
import io.github.shared.local.data.requests.Request;

public class DestroyRequest extends Request {
    ArrayList<Integer> entities;

    public DestroyRequest(){}
    public DestroyRequest(RequestType request) {
        super(request);
    }


}
