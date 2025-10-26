package io.github.shared.local.data.requests;

import io.github.shared.local.data.EnumsTypes.RequestType;

public class SpecialRequest extends Request{
    private String context;
    private Object obj;

    public SpecialRequest(){}
    public SpecialRequest(RequestType request) {
        super(request);
    }
}
