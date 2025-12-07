package io.github.core.data.component;


import com.artemis.PooledComponent;

import net.mgsx.gltf.scene3d.scene.Scene;


public class ModelComponent extends PooledComponent {
    public Scene scene;
    public ModelComponent() {}
    public ModelComponent(Scene s){ this.scene = s; }
    @Override public void reset(){ scene = null; }
}


