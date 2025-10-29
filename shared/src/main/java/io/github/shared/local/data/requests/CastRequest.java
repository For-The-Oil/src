package io.github.shared.local.data.requests;

import com.badlogic.gdx.math.Vector2;

import io.github.shared.local.data.EnumsTypes.ProjectileType;
import io.github.shared.local.data.EnumsTypes.RequestType;

public class CastRequest extends Request{
    private int from;
    private Vector2 target;
    private ProjectileType type;
    public CastRequest(){}
    public CastRequest(RequestType request) {
        super(request);
    }
}
