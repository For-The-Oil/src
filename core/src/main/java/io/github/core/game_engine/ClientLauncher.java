package io.github.core.game_engine;

import static io.github.shared.config.BaseGameConfig.FIXED_TIME_STEP;

import java.util.Collections;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicReference;

import io.github.core.data.ClientGame;
import io.github.core.game_engine.manager.GameManager;
import io.github.shared.data.NetGame;
import io.github.shared.data.enums_types.EntityType;
import io.github.shared.data.instructions.CreateInstruction;
import io.github.shared.data.instructions.Instruction;
import io.github.shared.shared_engine.manager.InstructionManager;

public class ClientLauncher extends Thread {
    private final Queue<Instruction> instructionSync;
    private final AtomicReference<NetGame> resyncRef;

    public ClientLauncher() {
        super("GameThread-" + ClientGame.getInstance().getGAME_UUID());
        this.instructionSync = new ConcurrentLinkedQueue<>();
        this.resyncRef = new AtomicReference<>(null);
        init();
    }

    private void init() {
        CreateInstruction createInstruction = new CreateInstruction(System.currentTimeMillis());
        for (int i = 0; i < 1; i++) {
            createInstruction.add(EntityType.TANK, null, i, -1, 80 * i, 80 * i, null);
        }
        ClientGame.getInstance().addQueueInstruction(Collections.singleton(createInstruction));
    }

    @Override
    public void run() {
        final ClientGame game = ClientGame.getInstance();
        System.out.println("Game loop started for game: " + game.getGAME_UUID());
        game.setLastTime(System.nanoTime());

        try {
            while (game.isRunning()) {
                double frameTimeMs = getTimeSinceLastFrameMs(game);
                game.setAccumulator(game.getAccumulator() + (float) frameTimeMs);

                while (game.getAccumulator() >= FIXED_TIME_STEP) {
                    if (game.getWorld() != null) {
                        // FIXED_TIME_STEP en millisecondes -> delta en secondes pour Artémis
                        game.getWorld().setDelta((float) (FIXED_TIME_STEP / 1000.0));
                        game.getWorld().process();
                    }

                    // Drainer les instructions reçues côté réseau de façon thread-safe
                    for (Instruction instr; (instr = instructionSync.poll()) != null;) {
                        game.getExecutionQueue().add(instr);
                    }

                    // Exécuter la file
                    while (!game.isEmptyExecutionQueue()) {
                        Instruction instruction = game.getExecutionQueue().poll();
                        if (instruction == null) continue;
                        InstructionManager.executeInstruction(instruction, game);
                    }

                    game.setAccumulator(game.getAccumulator() - (float) FIXED_TIME_STEP);
                }

                // FullGameResync, exécuté en dehors du tick mais à un point sûr
                NetGame toResync = resyncRef.getAndSet(null);
                if (toResync != null) {
                    GameManager.fullGameResync(toResync);
                }

                // Petite pause pour éviter le busy-wait
                if (game.getAccumulator() < FIXED_TIME_STEP) {
                    try {
                        Thread.sleep(0, 500_000); // ~0,5 ms
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        } catch (Throwable t) {
            t.printStackTrace(); // remplace par logger
        } finally {
            System.out.println("Game loop stopped for game: " + game.getGAME_UUID());
            System.out.println("Game loop stopped for game: " + game.isRunning());
        }
    }

    private double getTimeSinceLastFrameMs(ClientGame cg) {
        long now = System.nanoTime();
        long last = cg.getLastTime();
        cg.setLastTime(now);
        if (last == 0) return 0.0;
        return (now - last) / 1_000_000.0;
    }

    public void stopGame() {
        ClientGame.getInstance().stopRunning();
    }

    public void setResyncNetGame(NetGame resyncNetGame) {
        instructionSync.clear(); // TODO : vérifier l’impact (Paintest)
        resyncRef.set(resyncNetGame);
    }

    public void addQueueInstruction(java.util.Collection<Instruction> instructions) {
        instructionSync.addAll(instructions);
    }
}
