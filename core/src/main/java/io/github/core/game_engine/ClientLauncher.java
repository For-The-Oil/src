package io.github.core.game_engine;

import static io.github.shared.config.BaseGameConfig.FIXED_TIME_STEP;

import java.util.ArrayList;
import java.util.Collection;

import io.github.core.data.ClientGame;
import io.github.core.game_engine.manager.GameManager;
import io.github.shared.data.NetGame;
import io.github.shared.data.instructions.Instruction;
import io.github.shared.shared_engine.manager.InstructionManager;

public class ClientLauncher extends Thread {

    private NetGame resyncNetGame;
    private final Collection<Instruction> instructionSync;
    public ClientLauncher() {
        super("GameThread-" + ClientGame.getInstance().getGAME_UUID());
        instructionSync = new ArrayList<>();
        resyncNetGame = null;
        init();
    }
    private void init(){
        //ici start kryo avec this.serverGame.getRequestQueue(); en Paramètres pour récupérer les requests
    }


    @Override
    public void run() {
        System.out.println("Game loop started for game: " + ClientGame.getInstance().getGAME_UUID());
        while (ClientGame.getInstance().isRunning()) {
            float frameTime = getTimeSinceLastFrame();
            ClientGame.getInstance().setAccumulator(ClientGame.getInstance().getAccumulator() + frameTime);

            while (ClientGame.getInstance().getAccumulator() >= FIXED_TIME_STEP) {
                //Mise à jour ECS
                ClientGame.getInstance().getWorld().setDelta(FIXED_TIME_STEP / 1000f); // converti en secondes pour Artémis
                ClientGame.getInstance().getWorld().process();

                //addInstruction
                if(!instructionSync.isEmpty()) {
                    ClientGame.getInstance().addQueueInstruction(instructionSync);
                    instructionSync.clear();
                }

                //Exécuter les instructions en attente
                while (!ClientGame.getInstance().isEmptyExecutionQueue()) {
                    Instruction instruction = ClientGame.getInstance().getExecutionQueue().poll();
                    if(instruction == null)continue;
                    InstructionManager.executeInstruction(instruction, ClientGame.getInstance());
                }
                ClientGame.getInstance().setAccumulator(ClientGame.getInstance().getAccumulator() - FIXED_TIME_STEP);
            }

            //FullGameResync
            if(resyncNetGame != null){
                GameManager.fullGameResync(resyncNetGame);
                resyncNetGame = null;
            }
        }

        System.out.println("Game loop stopped for game: " + ClientGame.getInstance().getGAME_UUID());
    }

    private float getTimeSinceLastFrame() {
        long now = System.currentTimeMillis();
        float delta = now - ClientGame.getInstance().getLastTime();
        ClientGame.getInstance().setLastTime(now);
        return delta;
    }

    public void stopGame() {
        ClientGame.getInstance().stopRunning();
    }

    public void setResyncNetGame(NetGame resyncNetGame) {
        instructionSync.clear(); // TODO : Do Paintest
        this.resyncNetGame = resyncNetGame;
    }

    public void addQueueInstruction(Collection<Instruction> instruction){
        instructionSync.addAll(instruction);
    }
}
