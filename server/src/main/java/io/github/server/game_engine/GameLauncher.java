package io.github.server.game_engine;

import static io.github.shared.config.BaseGameConfig.FIXED_TIME_STEP;

import java.util.ArrayList;
import java.util.Collection;

import io.github.server.data.ServerGame;
import io.github.shared.data.instructions.Instruction;
import io.github.shared.data.requests.Request;
import io.github.shared.shared_engine.manager.InstructionManager;

public class GameLauncher extends Thread {

    private final ServerGame serverGame;

    private final Collection<Request> requestsSync;
    public GameLauncher(ServerGame serverGame) {
        super("GameThread-" + serverGame.getGAME_UUID());
        this.serverGame = serverGame;
        this.requestsSync = new ArrayList<>();
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

                //addInstruction
                if(!serverGame.getUpdateTracker().snapshotsIsEmpty()) serverGame.addQueueInstruction(serverGame.getUpdateTracker().consumeUpdateInstruction(System.currentTimeMillis()));
                if(!serverGame.destroyInstructionIsEmpty()) serverGame.addQueueInstruction(serverGame.consumeDestroyInstruction(System.currentTimeMillis()));
                if(!serverGame.createInstructionIsEmpty()) serverGame.addQueueInstruction(serverGame.consumeCreateInstruction(System.currentTimeMillis()));

                // Exécuter les instructions en attente
                while (!serverGame.isEmptyExecutionQueue()) {
                    Instruction instruction = serverGame.getExecutionQueue().poll();
                    if(instruction == null)continue;
                    InstructionManager.executeInstruction(instruction, serverGame);
                }
                serverGame.setAccumulator(serverGame.getAccumulator() - FIXED_TIME_STEP);
                serverGame.substractTime_left((long) FIXED_TIME_STEP);
            }

            //Traiter les requests
            serverGame.addQueueRequest(requestsSync);
            requestsSync.clear();
            while (!serverGame.isEmptyRequestQueue()) {
                Request request = serverGame.getRequestQueue().poll();
                if(request == null)continue;
                Instruction instruction = InstructionManager.executeGameRequest(request, serverGame);
                if(instruction == null)continue;
                serverGame.getHistoricQueue().add(instruction);
                serverGame.getNetworkQueue().add(instruction);
                InstructionManager.executeInstruction(instruction, serverGame);
            }

            //Envoi des instructions ici

            if(serverGame.getTime_left()<0)stopGame();
        }

        System.out.println("Game loop stopped for game: " + serverGame.getGAME_UUID());
    }

    public ServerGame getGame() {
        return serverGame;
    }

    public void stopGame() {
        serverGame.stopRunning();
    }

    public void addQueueRequest(Collection<Request> requests){
        requestsSync.addAll(requests);
    }

    private float getTimeSinceLastFrame() {
        long now = System.currentTimeMillis();
        float delta = now - serverGame.getLastTime();
        serverGame.setLastTime(now);
        return delta;
    }

}
