package io.github.shared.local.ecs.component;

import com.badlogic.ashley.core.Component;

public class MovementComponent implements Component {
    public final float base_speed = 0;
    public final float road_coef = 1;
    public final float debris_coef= 1;
    public final float water_coef= 1;
    public final float mud_coef= 1;

}
