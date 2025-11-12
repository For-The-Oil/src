package io.github.shared.local.data.requests.game;

import com.badlogic.gdx.math.GridPoint2;

import io.github.shared.local.data.EnumsTypes.EntityType;
import io.github.shared.local.data.EnumsTypes.RequestType;
import io.github.shared.local.data.requests.Request;

public class BuildRequest extends Request {
    private EntityType type;
    private int posX;
    private int posY;

    public BuildRequest(){}
    public BuildRequest(RequestType request) {
        super(request);
    }
}
