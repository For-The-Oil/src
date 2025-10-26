package io.github.shared.local.data.requests;

import io.github.shared.local.data.EnumsTypes.RequestType;
import io.github.shared.local.data.EnumsTypes.UnitType;

public class SummonRequest extends Request{

    private int from;
    private UnitType type;
    private int quantities;

    public SummonRequest(){}

    public SummonRequest(RequestType request) {
        super(request);
    }
}
