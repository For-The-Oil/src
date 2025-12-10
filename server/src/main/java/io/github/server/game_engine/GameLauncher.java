package io.github.server.game_engine;

import static io.github.shared.config.BaseGameConfig.FIXED_TIME_STEP;

import java.util.ArrayDeque;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import io.github.server.data.ServerGame;
import io.github.server.game_engine.manager.RequestGameManager;
import io.github.server.server_engine.manager.SyncManager;
import io.github.shared.data.instructions.Instruction;
import io.github.shared.data.requests.Request;
import io.github.shared.shared_engine.manager.InstructionManager;

public class GameLauncher extends Thread {
    private final ServerGame serverGame;
    private final Queue<Request> requestsSync;

    public GameLauncher(ServerGame serverGame) {
        super("GameThread-" + serverGame.getGAME_UUID());
        this.serverGame = serverGame;
        this.requestsSync = new ConcurrentLinkedQueue<>();
        init();
    }

    private void init() {
    }

    @Override
    public void run() {
        System.out.println("Game loop started for game: " + serverGame.getGAME_UUID());
        serverGame.setLastTime(System.nanoTime());
        try {
            while (serverGame.isRunning()) {
                double frameTimeMs = getTimeSinceLastFrameMs();
                serverGame.setAccumulator(serverGame.getAccumulator() + (float) frameTimeMs);

                while (serverGame.getAccumulator() >= FIXED_TIME_STEP) {
                    // FIXED_TIME_STEP en millisecondes -> delta en secondes pour Artémis
                    serverGame.getWorld().setDelta((float) (FIXED_TIME_STEP / 1000.0));
                    serverGame.getWorld().process();

                    if (!serverGame.getUpdateTracker().snapshotsIsEmpty()) serverGame.addQueueInstruction(serverGame.getUpdateTracker().consumeUpdateInstruction(System.currentTimeMillis(),serverGame.getWorld()));
                    if (!serverGame.destroyInstructionIsEmpty()) serverGame.addQueueInstruction(serverGame.consumeDestroyInstruction(System.currentTimeMillis()));
                    if (!serverGame.createInstructionIsEmpty()) serverGame.addQueueInstruction(serverGame.consumeCreateInstruction(System.currentTimeMillis()));

                    while (!serverGame.isEmptyExecutionQueue()) {
                        Instruction instruction = serverGame.getExecutionQueue().poll();
                        if (instruction == null) continue;
                        InstructionManager.executeInstruction(instruction, serverGame);
                    }

                    serverGame.setAccumulator(serverGame.getAccumulator() - (float) FIXED_TIME_STEP);
                    serverGame.substractTime_left(Math.round(FIXED_TIME_STEP)); // évite la dérive
                }

                // Drainer les requêtes arrivées de manière thread-safe
                if (!requestsSync.isEmpty()) {
                    for (Request r; (r = requestsSync.poll()) != null;) {
                        serverGame.getRequestQueue().add(r);
                    }
                }

                while (!serverGame.isEmptyRequestQueue()) {
                    Request request = serverGame.getRequestQueue().poll();
                    if (request == null) continue;
                    Instruction instruction = RequestGameManager.executeGameRequest(request, serverGame);
                    if (instruction == null) continue;
                    serverGame.getHistoricQueue().add(instruction);
                    serverGame.getNetworkQueue().add(instruction);
                    InstructionManager.executeInstruction(instruction, serverGame);
                }

                if (!serverGame.isEmptyNetworkQueue()) {
                    Queue<Instruction> toSend = new ArrayDeque<>();
                    for (Instruction ins; (ins = serverGame.getNetworkQueue().poll()) != null;) {
                        toSend.add(ins); // ins ne doit pas être null
                    }
                    if (!toSend.isEmpty()) {
                        SyncManager.sendInstructions(toSend, serverGame.getPlayersList());
                    }
                }

                // Petite pause pour éviter le busy-wait si rien à traiter
                if (serverGame.getAccumulator() < FIXED_TIME_STEP) {
                    try {
                        Thread.sleep(0, 500_000); // ~0,5 ms
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }

                if (serverGame.getTime_left() < 0) stopGame();
            }
        } catch (Throwable t) {
            t.printStackTrace();
        } finally {
            System.out.println("Game loop stopped for game: " + serverGame.getGAME_UUID());
        }
    }

    public ServerGame getGame() {
        return serverGame;
    }

    public void stopGame() {
        serverGame.stopRunning();
    }

    public void addQueueRequest(Request request) {
        requestsSync.add(request);
    }

    private double getTimeSinceLastFrameMs() {
        long now = System.nanoTime();
        long last = serverGame.getLastTime();
        serverGame.setLastTime(now);
        if (last == 0) return 0.0;
        return (now - last) / 1_000_000.0;
    }
}
