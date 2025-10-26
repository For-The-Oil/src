package io.github.shared.local.data.component;


import com.artemis.Component;
import com.badlogic.gdx.math.Quaternion;

import com.artemis.annotations.PooledWeaver;

@PooledWeaver
public class Angle3DComponent extends Component{
    public final Quaternion rotation = new Quaternion();
}
