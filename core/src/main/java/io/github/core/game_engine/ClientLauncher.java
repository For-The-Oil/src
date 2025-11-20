package io.github.core.game_engine;

import static io.github.shared.config.BaseGameConfig.FIXED_TIME_STEP;

import java.util.ArrayList;
import java.util.Collection;

import io.github.core.game_engine.manager.GameManager;
import io.github.shared.data.NetGame;
import io.github.shared.data.instructions.Instruction;
import io.github.shared.shared_engine.manager.InstructionManager;

public class ClientLauncher extends Thread {

    private ClientGame clientGame;
    private NetGame resyncNetGame;
    private final Collection<Instruction> instructionSync;
    public ClientLauncher(ClientGame clientGame) {
        super("GameThread-" + clientGame.getGAME_UUID());
        this.clientGame = clientGame;
        instructionSync = new ArrayList<>();
        resyncNetGame = null;
        init();
    }
    private void init(){
        //ici start kryo avec this.serverGame.getRequestQueue(); en Paramètres pour récupérer les requests
    }


    @Override
    public void run() {
        System.out.println("Game loop started for game: " + clientGame.getGAME_UUID());
        while (clientGame.isRunning()) {
            float frameTime = getTimeSinceLastFrame();
            clientGame.setAccumulator(clientGame.getAccumulator() + frameTime);

            while (clientGame.getAccumulator() >= FIXED_TIME_STEP) {
                //Mise à jour ECS
                clientGame.getWorld().setDelta(FIXED_TIME_STEP / 1000f); // converti en secondes pour Artémis
                clientGame.getWorld().process();

                //addInstruction
                clientGame.addQueueInstruction(instructionSync);
                instructionSync.clear();

                //Exécuter les instructions en attente
                while (!clientGame.isEmptyExecutionQueue()) {
                    Instruction instruction = clientGame.getExecutionQueue().poll();
                    if(instruction == null)continue;
                    InstructionManager.executeInstruction(instruction, clientGame);
                }
                clientGame.setAccumulator(clientGame.getAccumulator() - FIXED_TIME_STEP);
            }

            //FullGameResync
            if(resyncNetGame != null){
                ClientGame tmp = GameManager.fullGameResync(resyncNetGame,clientGame);
                if(tmp != null){
                    clientGame = tmp;
                }
            }
        }

        System.out.println("Game loop stopped for game: " + clientGame.getGAME_UUID());
    }

    private float getTimeSinceLastFrame() {
        long now = System.currentTimeMillis();
        float delta = now - clientGame.getLastTime();
        clientGame.setLastTime(now);
        return delta;
    }

    public void stopGame() {
        clientGame.stopRunning();
    }

    public void setResyncNetGame(NetGame resyncNetGame) {
        instructionSync.clear(); // TODO : Do Paintest
        this.resyncNetGame = resyncNetGame;
    }

    public void addQueueInstruction(Collection<Instruction> instruction){
        instructionSync.addAll(instruction);
    }
}
