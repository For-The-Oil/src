package io.github.server.game_engine.system;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.annotations.Wire;
import com.artemis.systems.IteratingSystem;


import io.github.server.data.ServerGame;
import io.github.shared.data.component.FreezeComponent;
import io.github.shared.data.component.LifeComponent;
import io.github.shared.data.component.NetComponent;
import io.github.shared.data.component.OnCreationComponent;
import io.github.shared.data.gameobject.DamageEntry;
import io.github.shared.data.snapshot.ComponentSnapshot;

@Wire
public class HealSystem extends IteratingSystem {

    private static final float PERIOD_SEC = 2.5f;

    // Injected component mappers
    private ComponentMapper<LifeComponent> mLife;
    private ComponentMapper<NetComponent> mNet;
    private final ServerGame server;

    private float accumulator = 0f;
    public HealSystem(ServerGame server) {
        super(Aspect.all(LifeComponent.class,NetComponent.class).exclude(FreezeComponent.class, OnCreationComponent.class));
        this.server = server;
    }

    /**
     * Adds the frame delta to the time accumulator.
     * Called once per frame before per-entity processing.
     */
    @Override
    protected void begin() {
        accumulator += world.getDelta();
    }


    @Override
    protected void process(int e) {
        if (accumulator < PERIOD_SEC) return;
        // Only Buildings produce resources
        LifeComponent life = mLife.get(e);
        if(life.passiveHeal > 0 && life.health != life.maxHealth) {

            NetComponent net = mNet.get(e);
            java.util.ArrayList<Object> entries = new java.util.ArrayList<>();
            entries.add(new DamageEntry(net.netId, -life.passiveHeal, 0)); // attackerId, raw damage, armor penetration

            java.util.HashMap<String, Object> fields = new java.util.HashMap<>();
            fields.put("entries", entries); // tracker will merge lists of entries per target

            ComponentSnapshot damageSnap = new ComponentSnapshot("DamageComponent", fields);
            server.getUpdateTracker().markComponentModified(world.getEntity(e), damageSnap);
        }
    }

    /**
     * Resets the accumulator after processing all entities in the frame.
     * Called once per frame after per-entity processing.
     */
    @Override
    protected void end() {
        if (accumulator >= PERIOD_SEC) {
            accumulator = 0f;
        }
    }
}
