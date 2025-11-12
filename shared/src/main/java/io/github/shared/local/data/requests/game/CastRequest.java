package io.github.shared.local.data.requests.game;

import com.badlogic.gdx.math.Vector2;

import io.github.shared.local.data.EnumsTypes.ProjectileType;
import io.github.shared.local.data.EnumsTypes.RequestType;
import io.github.shared.local.data.requests.Request;

public class CastRequest extends Request {
    private int from;
    private float targetX;
    private float targetY;
    private ProjectileType type;
    public CastRequest(){}
    public CastRequest(RequestType request) {
        super(request);
    }
}
