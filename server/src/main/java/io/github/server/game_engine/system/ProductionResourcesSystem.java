package io.github.server.game_engine.system;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.Entity;
import com.artemis.World;
import com.artemis.annotations.Wire;
import com.artemis.systems.IteratingSystem;

import java.util.HashMap;

import io.github.server.data.ServerGame;
import io.github.shared.data.component.FreezeComponent;
import io.github.shared.data.component.NetComponent;
import io.github.shared.data.component.OnCreationComponent;
import io.github.shared.data.component.ProprietyComponent;
import io.github.shared.data.component.RessourceComponent;
import io.github.shared.data.EnumsTypes.EntityType;
import io.github.shared.data.EnumsTypes.ResourcesType;
import io.github.shared.data.instructions.ResourcesInstruction;
import io.github.shared.data.network.Player;
import io.github.shared.shared_engine.Utility;

/**
 * ProductionResourcesSystem
 *
 * Periodically grants resource production from Building entities to their owner players.
 * Emits a ResourcesInstruction for the granted payload and marks the producing component
 * as modified for snapshot/update tracking.
 *
 * Responsibilities:
 * - Accumulate time and trigger production every PERIOD_SEC seconds.
 * - Filter entities to Buildings via NetComponent.entityType.getType().
 * - Resolve the owner Player using ProprietyComponent.player (UUID).
 * - Merge the entity’s production payload (RessourceComponent#getAll) into the owner’s inventory.
 * - Push a ResourcesInstruction for that production tick into the server queues.
 * - Mark RessourceComponent as modified to notify snapshot/update tracking.
 *
 * Assumptions:
 * - ServerGame#getPlayersList returns all connected players.
 * - Player#getRessources exposes a mutable map to merge into.
 * - RessourceComponent#getAll returns the per-period production payload.
 * - EntityType.Type.Building identifies producing structures.
 *
 * Timing:
 * - The system adds world.getDelta() to an internal accumulator in begin().
 * - When the accumulator reaches or exceeds PERIOD_SEC, each matching entity is
 *   processed once in that frame, and the accumulator is reset in end().
 */
@Wire
public class ProductionResourcesSystem extends IteratingSystem {

    /** Fixed production period in seconds. Adjust to your economy balance. */
    private static final float PERIOD_SEC = 5f;

    /** Server handle for queues, players, world, and snapshot tracking. */
    private final ServerGame game;

    // Injected component mappers
    private ComponentMapper<RessourceComponent> mRes;
    private ComponentMapper<ProprietyComponent> mProp;
    private ComponentMapper<NetComponent> mNet;

    /** Elapsed time since the last production pass. */
    private float accumulator = 0f;

    /**
     * Creates a production system bound to a ServerGame.
     *
     * @param game the server game context used for player access and update tracking
     */
    public ProductionResourcesSystem(ServerGame game) {
        super(Aspect.all(RessourceComponent.class, ProprietyComponent.class, NetComponent.class).exclude(FreezeComponent.class, OnCreationComponent.class));
        this.game = game;
    }

    /**
     * Adds the frame delta to the time accumulator.
     * Called once per frame before per-entity processing.
     */
    @Override
    protected void begin() {
        accumulator += world.getDelta();
    }

    /**
     * Executes production for a single entity when the period elapses.
     * Steps:
     * 1) Verify the entity is a Building.
     * 2) Resolve the owner player by ProprietyComponent.player.
     * 3) Read the payload from RessourceComponent#getAll.
     * 4) Emit a ResourcesInstruction describing the granted resources.
     *
     * If the period has not elapsed yet, the method returns immediately.
     *
     * @param e Artemis entity identifier being processed
     */
    @Override
    protected void process(int e) {
        if (accumulator < PERIOD_SEC) return;

        // Only Buildings produce resources
        NetComponent net = mNet.get(e);
        if (net == null || net.entityType == null) return;
        if (!EntityType.Type.Building.equals(net.entityType.getType())) return;

        // Resolve owner
        ProprietyComponent prop = mProp.get(e);
        if (prop == null || prop.player == null) return;

        Player owner = Utility.findPlayerByUuid(game.getPlayersList(), prop.player);
        if (owner == null) return;

        // Read payload and merge into the player's inventory
        RessourceComponent res = mRes.get(e);
        if (res == null) return;

        HashMap<ResourcesType, Integer> payload = res.getAll();
        if (payload == null || payload.isEmpty()) return;

        // Emit one ResourcesInstruction for this production tick
        long timestamp = System.currentTimeMillis();
        ResourcesInstruction instruction = new ResourcesInstruction(timestamp, payload, prop.player);
        game.addQueueInstruction(instruction);
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
