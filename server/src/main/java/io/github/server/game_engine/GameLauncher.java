package io.github.server.game_engine;

import static io.github.server.config.BaseGameConfig.FIXED_TIME_STEP;

import io.github.server.data.Game;
import io.github.shared.local.data.instructions.Instruction;
import io.github.shared.local.shared_engine.manager.InstructionManager;

public class GameLauncher extends Thread {

    private final Game game;
    public GameLauncher(Game game) {
        super("GameThread-" + game.getGAME_UUID());
        this.game = game;
        init();
    }
    private void init(){
    }


    @Override
    public void run() {
        System.out.println("Game loop started for game: " + game.getGAME_UUID());
        while (game.isRunning()) {
            float frameTime = getTimeSinceLastFrame();
            game.setAccumulator(game.getAccumulator() + frameTime);

            while (game.getAccumulator() >= FIXED_TIME_STEP) {
//          Exécuter les instructions en attente
                while (!game.isEmptyExecutionQueue()) {
                    Instruction instruction = game.getExecutionQueue().poll();
                    if(instruction == null)continue;
                    InstructionManager.executeInstruction(instruction,game);
                }

//          Mise à jour ECS
                game.getWorld().setDelta(FIXED_TIME_STEP / 1000f); // converti en secondes pour Artémis
                game.getWorld().process();

                game.setAccumulator(game.getAccumulator() - FIXED_TIME_STEP);
            }

//       Envoi des snapshots ici
        }

        System.out.println("Game loop stopped for game: " + game.getGAME_UUID());
    }

    private float getTimeSinceLastFrame() {
        long now = System.currentTimeMillis();
        float delta = now - game.getLastTime();
        game.setLastTime(now);
        return delta;
    }

    public void stopGame() {
        game.stopRunning();
    }

    public Game getGame() {
        return game;
    }
}
