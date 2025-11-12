package io.github.server.game_engine;

import static io.github.server.config.BaseGameConfig.FIXED_TIME_STEP;

import com.badlogic.gdx.Game;

import io.github.server.data.ServerGame;
import io.github.shared.local.data.instructions.Instruction;
import io.github.shared.local.shared_engine.manager.InstructionManager;

public class GameLauncher extends Thread {

    private final ServerGame serverGame;
    public GameLauncher(ServerGame serverGame) {
        super("GameThread-" + serverGame.getGAME_UUID());
        this.serverGame = serverGame;
        init();
    }
    private void init(){
        //ici start kryo avec this.serverGame.getRequestQueue(); en Paramètres pour récupérer les requests
    }


    @Override
    public void run() {
        System.out.println("Game loop started for game: " + serverGame.getGAME_UUID());
        while (serverGame.isRunning()) {
            float frameTime = getTimeSinceLastFrame();
            serverGame.setAccumulator(serverGame.getAccumulator() + frameTime);

            while (serverGame.getAccumulator() >= FIXED_TIME_STEP) {
                //Mise à jour ECS
                serverGame.getWorld().setDelta(FIXED_TIME_STEP / 1000f); // converti en secondes pour Artémis
                serverGame.getWorld().process();

                //consumeSnapshots
                serverGame.addQueueInstruction(serverGame.getSnapshotTracker().createUpdateInstruction(System.currentTimeMillis()));

                // Exécuter les instructions en attente
                while (!serverGame.isEmptyExecutionQueue()) {
                    Instruction instruction = serverGame.getExecutionQueue().poll();
                    if(instruction == null)continue;
                    InstructionManager.executeInstruction(instruction, serverGame);
                }
                serverGame.setAccumulator(serverGame.getAccumulator() - FIXED_TIME_STEP);
            }

//       Envoi des instructions ici
//       Traiter les requests  ici
        }

        System.out.println("Game loop stopped for game: " + serverGame.getGAME_UUID());
    }

    private float getTimeSinceLastFrame() {
        long now = System.currentTimeMillis();
        float delta = now - serverGame.getLastTime();
        serverGame.setLastTime(now);
        return delta;
    }

    public void stopGame() {
        serverGame.stopRunning();
    }

    public ServerGame getGame() {
        return serverGame;
    }
}
