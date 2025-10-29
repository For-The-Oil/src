package io.github.shared.local.data.requests;

import com.badlogic.gdx.math.GridPoint2;

import io.github.shared.local.data.EnumsTypes.EntityType;
import io.github.shared.local.data.EnumsTypes.RequestType;

public class BuildRequest extends Request{
    private EntityType type;
    private GridPoint2 pos;

    public BuildRequest(){}
    public BuildRequest(RequestType request) {
        super(request);
    }
}
