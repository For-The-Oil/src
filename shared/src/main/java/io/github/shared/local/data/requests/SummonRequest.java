package io.github.shared.local.data.requests;

import io.github.shared.local.data.EnumsTypes.RequestType;
import io.github.shared.local.data.EnumsTypes.EntityType;

public class SummonRequest extends Request{

    private int from;
    private EntityType type;
    private int quantities;

    public SummonRequest(){}

    public SummonRequest(RequestType request) {
        super(request);
    }
}
