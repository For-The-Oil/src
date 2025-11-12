package io.github.shared.local.data.requests.game;

import io.github.shared.local.data.EnumsTypes.RequestType;
import io.github.shared.local.data.EnumsTypes.EntityType;
import io.github.shared.local.data.requests.Request;

public class SummonRequest extends Request {
    private EntityType type;
    private int from;
    private int quantities;

    public SummonRequest(){}

    public SummonRequest(RequestType request) {
        super(request);
    }
}
